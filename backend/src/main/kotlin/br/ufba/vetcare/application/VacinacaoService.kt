package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.VacinacaoRequest
import br.ufba.vetcare.api.dto.VacinacaoResposta
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Vacinacao
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import br.ufba.vetcare.domain.repository.VacinacaoRepository
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * REQ12 - Registrar Vacinação, incluindo a previsão da próxima dose.
 */
@Service
class VacinacaoService(
    private val repository: VacinacaoRepository,
    private val animalRepository: AnimalRepository,
    private val usuarioRepository: UsuarioRepository,
    private val consultaRepository: ConsultaRepository,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listar(animalId: Long, veterinarioId: Long? = null): List<VacinacaoResposta> {
        if (!animalRepository.existsById(animalId)) {
            throw RecursoNaoEncontradoException("Animal não encontrado.")
        }
        if (veterinarioId != null && !consultaRepository.existsByAnimalIdAndVeterinarioId(animalId, veterinarioId)) {
            auditoriaService.registrarIndependente(
                acao = "VACINACAO_ACESSO_NEGADO",
                entidade = "Animal",
                entidadeId = animalId,
                detalhes = "Tentativa de acesso a vacinação de paciente fora da agenda",
            )
            throw org.springframework.security.access.AccessDeniedException(
                "Este paciente não faz parte da sua agenda clínica.",
            )
        }
        return repository.findByAnimalIdOrderByDataAplicacaoDesc(animalId).map(VacinacaoResposta::de)
    }

    @Transactional
    @CacheEvict(cacheNames = ["prontuario"], allEntries = true)
    fun criar(animalId: Long, request: VacinacaoRequest, solicitante: UsuarioAutenticado): VacinacaoResposta {
        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Animal não encontrado.") }
        if (solicitante.perfil == Perfil.VETERINARIO &&
            !consultaRepository.existsByAnimalIdAndVeterinarioId(animalId, solicitante.id)
        ) {
            auditoriaService.registrarIndependente(
                acao = "VACINACAO_ACESSO_NEGADO",
                entidade = "Animal",
                entidadeId = animalId,
                detalhes = "Tentativa de registrar vacina em paciente fora da agenda",
            )
            throw org.springframework.security.access.AccessDeniedException(
                "Este paciente não faz parte da sua agenda clínica.",
            )
        }
        val dataAplicacao = request.dataAplicacao
            ?: throw RegraNegocioException("Informe a data de aplicação.")
        if (dataAplicacao.isAfter(LocalDate.now())) {
            throw RegraNegocioException("A data de aplicação não pode estar no futuro.")
        }
        request.proximaDose?.let {
            if (it.isBefore(dataAplicacao)) {
                throw RegraNegocioException("A próxima dose deve ser posterior à aplicação.")
            }
        }
        val veterinario = if (solicitante.perfil == Perfil.VETERINARIO) {
            usuarioRepository.findById(solicitante.id).orElse(null)
        } else {
            null
        }
        val vacinacao = repository.save(
            Vacinacao(
                animal = animal,
                veterinario = veterinario,
                tipoVacina = request.tipoVacina.trim(),
                lote = request.lote?.trim()?.takeIf { it.isNotEmpty() },
                dataAplicacao = dataAplicacao,
                proximaDose = request.proximaDose,
                observacoes = request.observacoes?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
        auditoriaService.registrar(
            acao = "VACINACAO_REGISTRADA",
            entidade = "Vacinacao",
            entidadeId = vacinacao.id,
            detalhes = "${vacinacao.tipoVacina} em ${animal.nome}",
        )
        return VacinacaoResposta.de(vacinacao)
    }

    @Transactional
    @CacheEvict(cacheNames = ["prontuario"], allEntries = true)
    fun excluir(id: Long) {
        val vacinacao = repository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Vacinação não encontrada.") }
        repository.delete(vacinacao)
        auditoriaService.registrar(
            acao = "VACINACAO_EXCLUIDA",
            entidade = "Vacinacao",
            entidadeId = id,
        )
    }
}
