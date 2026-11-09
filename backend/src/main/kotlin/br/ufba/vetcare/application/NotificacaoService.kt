package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.NotificacaoResposta
import br.ufba.vetcare.api.dto.ProcessamentoNotificacoesResposta
import br.ufba.vetcare.domain.Notificacao
import br.ufba.vetcare.domain.StatusConsulta
import br.ufba.vetcare.domain.StatusNotificacao
import br.ufba.vetcare.domain.TipoNotificacao
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.NotificacaoRepository
import br.ufba.vetcare.domain.repository.VacinacaoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * REQ16 - Gerar e enviar avisos ao tutor sobre consultas e vacinação.
 * O canal é abstraído por CanalNotificacao; nesta sprint usa-se um adaptador simulado.
 */
@Service
class NotificacaoService(
    private val repository: NotificacaoRepository,
    private val consultaRepository: ConsultaRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val canal: CanalNotificacao,
    private val auditoriaService: AuditoriaService,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @Transactional(readOnly = true)
    fun listar(): List<NotificacaoResposta> =
        repository.findAllByOrderByCriadoEmDesc().map(NotificacaoResposta::de)

    @Transactional
    fun gerarAvisos(): Int {
        var geradas = 0
        val agora = LocalDateTime.now()
        val consultas = consultaRepository.listarAgenda(agora, agora.plusHours(24), null)
            .filter { it.status == StatusConsulta.AGENDADA }

        for (consulta in consultas) {
            if (repository.existsByTipoAndConsultaId(TipoNotificacao.CONSULTA, consulta.id!!)) continue
            repository.save(
                Notificacao(
                    tutor = consulta.tutor,
                    consulta = consulta,
                    tipo = TipoNotificacao.CONSULTA,
                    destinatario = consulta.tutor.email,
                    mensagem = "Olá, ${consulta.tutor.nome}! Lembrete: ${consulta.animal.nome} tem " +
                        "consulta em ${consulta.dataHora.format(formato)} com ${consulta.veterinario.nome}.",
                ),
            )
            geradas++
        }

        val hoje = LocalDate.now()
        val vacinas = vacinacaoRepository.findByProximaDoseBetween(hoje, hoje.plusDays(7))
        for (vacinacao in vacinas) {
            if (repository.existsByTipoAndVacinacaoId(TipoNotificacao.VACINA, vacinacao.id!!)) continue
            repository.save(
                Notificacao(
                    tutor = vacinacao.animal.tutor,
                    vacinacao = vacinacao,
                    tipo = TipoNotificacao.VACINA,
                    destinatario = vacinacao.animal.tutor.email,
                    mensagem = "Olá, ${vacinacao.animal.tutor.nome}! A próxima dose de " +
                        "${vacinacao.tipoVacina} de ${vacinacao.animal.nome} está prevista para " +
                        "${vacinacao.proximaDose!!.format(formatoData)}.",
                ),
            )
            geradas++
        }
        return geradas
    }

    @Transactional
    fun processarPendentes(): Int {
        val pendentes = repository.findByStatusOrderByCriadoEm(StatusNotificacao.PENDENTE)
        var enviadas = 0
        for (notificacao in pendentes) {
            try {
                canal.enviar(notificacao.destinatario, notificacao.mensagem)
                notificacao.status = StatusNotificacao.ENVIADA
                notificacao.enviadoEm = Instant.now()
                notificacao.erro = null
                enviadas++
            } catch (ex: Exception) {
                notificacao.status = StatusNotificacao.FALHA
                notificacao.erro = ex.message?.take(300)
                log.warn("Falha ao enviar notificação {}: {}", notificacao.id, ex.message)
            }
        }
        repository.saveAll(pendentes)
        if (enviadas > 0) {
            auditoriaService.registrar(
                acao = "NOTIFICACOES_ENVIADAS",
                entidade = "Notificacao",
                detalhes = "$enviadas avisos enviados",
            )
        }
        return enviadas
    }

    @Transactional
    fun processarTudo(): ProcessamentoNotificacoesResposta {
        val geradas = gerarAvisos()
        val enviadas = processarPendentes()
        return ProcessamentoNotificacoesResposta(geradas = geradas, enviadas = enviadas)
    }
}
