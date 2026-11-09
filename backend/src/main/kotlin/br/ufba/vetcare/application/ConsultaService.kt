package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.ConsultaRequest
import br.ufba.vetcare.api.dto.ConsultaResposta
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Consulta
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.StatusConsulta
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConsultaService(
    private val repository: ConsultaRepository,
    private val animalRepository: AnimalRepository,
    private val tutorRepository: TutorRepository,
    private val usuarioRepository: UsuarioRepository,
    private val auditoriaService: AuditoriaService,
) {

    /**
     * REQ07 - Agendar Consulta. Valida vínculo animal/tutor e conflito de horário do veterinário.
     */
    @Transactional
    @CacheEvict(cacheNames = ["agenda"], allEntries = true)
    fun agendar(request: ConsultaRequest): ConsultaResposta {
        val animalId = request.animalId ?: throw RegraNegocioException("Selecione o animal.")
        val tutorId = request.tutorId ?: throw RegraNegocioException("Selecione o tutor.")
        val veterinarioId = request.veterinarioId ?: throw RegraNegocioException("Selecione o veterinário.")
        val dataHora = request.dataHora ?: throw RegraNegocioException("Informe a data e o horário.")

        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Animal não encontrado.") }
        if (!animal.ativo) {
            throw RegraNegocioException("O animal informado está inativo.")
        }
        val tutor = tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Tutor não encontrado.") }
        if (animal.tutor.id != tutor.id) {
            throw RegraNegocioException("O animal selecionado não pertence ao tutor informado.")
        }
        val veterinario = usuarioRepository.findById(veterinarioId)
            .orElseThrow { RecursoNaoEncontradoException("Veterinário não encontrado.") }
        if (veterinario.perfil != Perfil.VETERINARIO) {
            throw RegraNegocioException("O profissional selecionado não é um veterinário.")
        }
        if (!veterinario.ativo) {
            throw RegraNegocioException("O veterinário informado está inativo.")
        }
        if (!dataHora.isAfter(LocalDateTime.now())) {
            throw RegraNegocioException("A consulta deve ser agendada para uma data e horário futuros.")
        }
        if (repository.existsByVeterinarioIdAndDataHoraAndStatusNot(veterinarioId, dataHora, StatusConsulta.CANCELADA)) {
            throw ConflitoException("O veterinário já possui uma consulta neste horário.")
        }

        val consulta = repository.save(
            Consulta(
                animal = animal,
                tutor = tutor,
                veterinario = veterinario,
                dataHora = dataHora,
                observacoes = request.observacoes?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
        auditoriaService.registrar(
            acao = "CONSULTA_AGENDADA",
            entidade = "Consulta",
            entidadeId = consulta.id,
            detalhes = "Animal ${animal.nome} com ${veterinario.nome} em $dataHora",
        )
        return ConsultaResposta.de(consulta)
    }

    /**
     * Visão de agenda usada pela Sprint 1 para comprovar o REQ07. A consulta completa
     * por dia/semana/profissional (REQ09) fica para a próxima sprint.
     */
    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = ["agenda"],
        key = "#inicio.toString() + ':' + #fim.toString() + ':' + (#veterinarioId ?: 0)",
    )
    fun listarAgenda(inicio: LocalDateTime, fim: LocalDateTime, veterinarioId: Long?): List<ConsultaResposta> {
        if (fim.isBefore(inicio)) {
            throw RegraNegocioException("O fim do período deve ser posterior ao início.")
        }
        return repository.listarAgenda(inicio, fim, veterinarioId).map(ConsultaResposta::de)
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long, veterinarioId: Long? = null): ConsultaResposta {
        val consulta = if (veterinarioId != null) {
            repository.findByIdAndVeterinarioId(id, veterinarioId)
        } else {
            repository.findById(id).orElse(null)
        } ?: throw RecursoNaoEncontradoException("Consulta não encontrada.")
        return ConsultaResposta.de(consulta)
    }
}
