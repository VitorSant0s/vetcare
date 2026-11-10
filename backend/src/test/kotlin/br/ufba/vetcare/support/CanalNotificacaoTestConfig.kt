package br.ufba.vetcare.support

import br.ufba.vetcare.application.CanalNotificacao
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Dublê de teste: não envia e-mail real, apenas permite validar a caixa de saída.
 */
@TestConfiguration
class CanalNotificacaoTestConfig {
    @Bean
    @Primary
    fun canalNotificacaoTeste(): CanalNotificacao = object : CanalNotificacao {
        override fun enviar(destinatario: String, mensagem: String) = Unit
    }
}
