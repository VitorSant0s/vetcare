package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.ProntuarioResposta
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * REQ11 - Prontuário: autorização e auditoria em toda leitura.
 * O conteúdo clínico vem do cache Redis; qualquer mutação invalida a entrada.
 */
@Service
class ProntuarioService(
    private val animalRepository: AnimalRepository,
    private val consultaRepository: ConsultaRepository,
    private val cacheService: ProntuarioCacheService,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional
    fun montar(animalId: Long, veterinarioId: Long? = null): ProntuarioResposta {
        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Animal não encontrado.") }
        if (veterinarioId != null && !consultaRepository.existsByAnimalIdAndVeterinarioId(animalId, veterinarioId)) {
            auditoriaService.registrarIndependente(
                acao = "PRONTUARIO_ACESSO_NEGADO",
                entidade = "Animal",
                entidadeId = animalId,
                detalhes = "Tentativa de acesso a paciente fora da agenda do veterinário",
            )
            throw org.springframework.security.access.AccessDeniedException(
                "Este paciente não faz parte da sua agenda clínica.",
            )
        }

        val prontuario = cacheService.carregar(animalId)

        auditoriaService.registrar(
            acao = "PRONTUARIO_ACESSADO",
            entidade = "Animal",
            entidadeId = animalId,
            detalhes = animal.nome,
        )
        return prontuario
    }
}
