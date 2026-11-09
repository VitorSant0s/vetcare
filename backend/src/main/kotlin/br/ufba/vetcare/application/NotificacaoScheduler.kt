package br.ufba.vetcare.application

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Gera e envia os avisos diariamente às 08:00 (ADR-06: processamento fora do fluxo do atendimento).
 */
@Component
@ConditionalOnProperty(prefix = "app.notificacoes", name = ["agendamento"], havingValue = "true", matchIfMissing = true)
class NotificacaoScheduler(private val service: NotificacaoService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 8 * * *")
    fun executar() {
        val resultado = service.processarTudo()
        log.info("Notificações diárias: {} geradas, {} enviadas", resultado.geradas, resultado.enviadas)
    }
}
