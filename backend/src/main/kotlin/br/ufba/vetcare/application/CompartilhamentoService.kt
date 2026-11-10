package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.CompartilhamentoResposta
import br.ufba.vetcare.api.dto.ProntuarioResposta
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.CompartilhamentoProntuario
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.CompartilhamentoProntuarioRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Compartilhamento do prontuário por código temporário entre veterinários.
 */
@Service
class CompartilhamentoService(
    private val repository: CompartilhamentoProntuarioRepository,
    private val animalRepository: AnimalRepository,
    private val consultaRepository: ConsultaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val prontuarioService: ProntuarioService,
    private val auditoriaService: AuditoriaService,
) {

    private val random = SecureRandom()
    private val alfabeto = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    @Transactional
    fun compartilhar(animalId: Long, veterinarioId: Long): CompartilhamentoResposta {
        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Animal não encontrado.") }
        if (!consultaRepository.existsByAnimalIdAndVeterinarioId(animalId, veterinarioId)) {
            throw AccessDeniedException("Você só pode compartilhar prontuários dos seus pacientes.")
        }
        val veterinario = usuarioRepository.findById(veterinarioId)
            .orElseThrow { RecursoNaoEncontradoException("Veterinário não encontrado.") }

        // Invalida códigos anteriores do mesmo animal.
        repository.findByAnimalIdAndAtivoTrueOrderByCriadoEmDesc(animalId).forEach {
            it.ativo = false
            repository.save(it)
        }

        val compartilhamento = repository.save(
            CompartilhamentoProntuario(
                animal = animal,
                criadoPor = veterinario,
                codigo = gerarCodigoUnico(),
                expiraEm = Instant.now().plus(7, ChronoUnit.DAYS),
            ),
        )
        auditoriaService.registrar(
            acao = "PRONTUARIO_COMPARTILHADO",
            entidade = "Animal",
            entidadeId = animalId,
            detalhes = "Código ${compartilhamento.codigo} válido até ${compartilhamento.expiraEm}",
        )
        return CompartilhamentoResposta(
            id = compartilhamento.id ?: 0,
            animalId = animalId,
            animalNome = animal.nome,
            codigo = compartilhamento.codigo,
            expiraEm = compartilhamento.expiraEm,
            criadoEm = compartilhamento.criadoEm,
            criadoPorNome = veterinario.nome,
        )
    }

    @Transactional
    fun buscarPorCodigo(codigo: String): ProntuarioResposta {
        val compartilhamento = repository.findByCodigoIgnoreCase(codigo.trim()).orElse(null)
        if (compartilhamento == null) {
            auditoriaService.registrarIndependente(
                acao = "PRONTUARIO_CODIGO_INVALIDO",
                entidade = "Compartilhamento",
                detalhes = "Código informado: ${codigo.take(12)}",
            )
            throw RecursoNaoEncontradoException("Código de prontuário não encontrado.")
        }
        if (!compartilhamento.ativo) {
            auditoriaService.registrarIndependente(
                acao = "PRONTUARIO_CODIGO_REVOGADO",
                entidade = "Animal",
                entidadeId = compartilhamento.animal.id,
                detalhes = "Código ${compartilhamento.codigo}",
            )
            throw RegraNegocioException("Este código foi revogado. Peça um novo compartilhamento.")
        }
        if (compartilhamento.expiraEm.isBefore(Instant.now())) {
            auditoriaService.registrarIndependente(
                acao = "PRONTUARIO_CODIGO_EXPIRADO",
                entidade = "Animal",
                entidadeId = compartilhamento.animal.id,
                detalhes = "Código ${compartilhamento.codigo}",
            )
            throw RegraNegocioException("Este código expirou. Peça um novo compartilhamento.")
        }
        val prontuario = prontuarioService.montar(compartilhamento.animal.id!!, null)
        auditoriaService.registrar(
            acao = "PRONTUARIO_COMPARTILHADO_ACESSADO",
            entidade = "Animal",
            entidadeId = compartilhamento.animal.id,
            detalhes = "Código ${compartilhamento.codigo}",
        )
        return prontuario
    }

    @Transactional
    fun revogar(animalId: Long, veterinarioId: Long) {
        if (!consultaRepository.existsByAnimalIdAndVeterinarioId(animalId, veterinarioId)) {
            throw AccessDeniedException("Você só pode revogar códigos dos seus pacientes.")
        }
        repository.findByAnimalIdAndAtivoTrueOrderByCriadoEmDesc(animalId).forEach {
            it.ativo = false
            repository.save(it)
        }
        auditoriaService.registrar(
            acao = "PRONTUARIO_COMPARTILHAMENTO_REVOGADO",
            entidade = "Animal",
            entidadeId = animalId,
        )
    }

    private fun gerarCodigoUnico(): String {
        repeat(20) {
            val codigo = (1..8).map { alfabeto[random.nextInt(alfabeto.length)] }.joinToString("")
            if (!repository.existsByCodigo(codigo)) return codigo
        }
        throw RegraNegocioException("Não foi possível gerar um código. Tente novamente.")
    }
}
