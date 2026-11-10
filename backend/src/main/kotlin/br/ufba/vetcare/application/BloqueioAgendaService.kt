package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.BloqueioAgendaRequest
import br.ufba.vetcare.api.dto.BloqueioAgendaResposta
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.BloqueioAgenda
import br.ufba.vetcare.domain.Notificacao
import br.ufba.vetcare.domain.StatusConsulta
import br.ufba.vetcare.domain.TipoNotificacao
import br.ufba.vetcare.domain.repository.BloqueioAgendaRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.NotificacaoRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Bloqueio de um dia de trabalho do veterinário. As consultas agendadas nesse dia
 * são canceladas e os tutores entram na fila de notificação.
 */
@Service
class BloqueioAgendaService(
    private val repository: BloqueioAgendaRepository,
    private val consultaRepository: ConsultaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val notificacaoRepository: NotificacaoRepository,
    private val auditoriaService: AuditoriaService,
) {

    private val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @Transactional(readOnly = true)
    fun listar(veterinarioId: Long, inicio: LocalDate?, fim: LocalDate?): List<BloqueioAgendaResposta> {
        val de = inicio ?: LocalDate.now()
        val ate = fim ?: de.plusDays(90)
        if (ate.isBefore(de)) {
            throw RegraNegocioException("A data final deve ser posterior à inicial.")
        }
        return repository.findByVeterinarioIdAndDataBetweenOrderByDataAsc(veterinarioId, de, ate)
            .map { BloqueioAgendaResposta.de(it) }
    }

    @Transactional
    @CacheEvict(cacheNames = ["agenda"], allEntries = true)
    fun bloquear(veterinarioId: Long, request: BloqueioAgendaRequest): BloqueioAgendaResposta {
        val data = request.data ?: throw RegraNegocioException("Informe a data do bloqueio.")
        if (data.isBefore(LocalDate.now())) {
            throw RegraNegocioException("Não é possível bloquear uma data passada.")
        }
        if (repository.existsByVeterinarioIdAndData(veterinarioId, data)) {
            throw ConflitoException("Este dia já está bloqueado.")
        }
        val veterinario = usuarioRepository.findById(veterinarioId)
            .orElseThrow { RecursoNaoEncontradoException("Veterinário não encontrado.") }

        val bloqueio = repository.save(
            BloqueioAgenda(
                veterinario = veterinario,
                data = data,
                motivo = request.motivo.trim(),
            ),
        )

        val consultas = consultaRepository
            .listarAgenda(data.atStartOfDay(), data.plusDays(1).atStartOfDay(), veterinarioId)
            .filter { it.status == StatusConsulta.AGENDADA }

        for (consulta in consultas) {
            consulta.status = StatusConsulta.CANCELADA
            consulta.horarioAtivo = null
            consulta.motivoCancelamento = "Agenda bloqueada: ${bloqueio.motivo}"
            consulta.canceladaEm = Instant.now()
            consultaRepository.save(consulta)
            auditoriaService.registrar(
                acao = "CONSULTA_CANCELADA_POR_BLOQUEIO",
                entidade = "Consulta",
                entidadeId = consulta.id,
                detalhes = "Dia ${data.format(formatoData)}: ${bloqueio.motivo}",
            )
            notificacaoRepository.save(
                Notificacao(
                    tutor = consulta.tutor,
                    consulta = consulta,
                    tipo = TipoNotificacao.CONSULTA,
                    destinatario = consulta.tutor.email,
                    mensagem = "Olá, ${consulta.tutor.nome}! A consulta de ${consulta.animal.nome} em " +
                        "${data.format(formatoData)} foi cancelada porque o veterinário bloqueou o dia: " +
                        "${bloqueio.motivo}.",
                ),
            )
        }

        auditoriaService.registrar(
            acao = "AGENDA_BLOQUEADA",
            entidade = "BloqueioAgenda",
            entidadeId = bloqueio.id,
            detalhes = "${data.format(formatoData)} · ${bloqueio.motivo} · ${consultas.size} consulta(s) cancelada(s)",
        )
        return BloqueioAgendaResposta.de(bloqueio, consultas.size)
    }

    @Transactional
    @CacheEvict(cacheNames = ["agenda"], allEntries = true)
    fun desbloquear(veterinarioId: Long, data: LocalDate) {
        val bloqueio = repository.findByVeterinarioIdAndData(veterinarioId, data)
            .orElseThrow { RecursoNaoEncontradoException("Bloqueio não encontrado.") }
        repository.delete(bloqueio)
        auditoriaService.registrar(
            acao = "AGENDA_DESBLOQUEADA",
            entidade = "BloqueioAgenda",
            entidadeId = bloqueio.id,
            detalhes = data.format(formatoData),
        )
    }
}
