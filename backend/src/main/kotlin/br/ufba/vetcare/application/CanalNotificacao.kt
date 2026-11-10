package br.ufba.vetcare.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

/**
 * Porta de saída para canais externos de notificação (ADR-15).
 */
interface CanalNotificacao {
    fun enviar(destinatario: String, mensagem: String)
}

/**
 * Envio real por SMTP, configurado por MAIL_HOST, MAIL_PORT, MAIL_USERNAME,
 * MAIL_PASSWORD, MAIL_FROM e NOTIFICACOES_CANAL=SMTP.
 */
@Component
@ConditionalOnProperty(
    prefix = "app.notificacoes",
    name = ["canal"],
    havingValue = "SMTP",
    matchIfMissing = true,
)
class SmtpCanalNotificacao(
    private val mailSender: JavaMailSender,
    @Value("\${app.notificacoes.remetente:no-reply@vetcare.local}") private val remetente: String,
) : CanalNotificacao {

    override fun enviar(destinatario: String, mensagem: String) {
        val email = SimpleMailMessage()
        email.from = remetente
        email.setTo(destinatario)
        email.subject = "VetCare - aviso da clínica"
        email.text = mensagem
        mailSender.send(email)
    }
}
