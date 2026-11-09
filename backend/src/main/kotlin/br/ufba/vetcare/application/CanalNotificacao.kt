package br.ufba.vetcare.application

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Porta de saída para canais externos de notificação (ADR-15).
 */
interface CanalNotificacao {
    fun enviar(destinatario: String, mensagem: String)
}

/**
 * Adaptador de desenvolvimento: registra o envio no log em vez de chamar um provedor real.
 */
@Component
class EmailSimuladoCanal : CanalNotificacao {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun enviar(destinatario: String, mensagem: String) {
        log.info("[EMAIL SIMULADO] Para: {} | {}", destinatario, mensagem)
    }
}
