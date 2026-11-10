package br.ufba.vetcare.application

import br.ufba.vetcare.application.exception.TooManyRequestsException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Proteção contra força bruta: limita tentativas de login por IP + identificador.
 */
@Service
class LoginRateLimiter(
    @Value("\${app.security.rate-limit.enabled:true}") private val habilitado: Boolean,
    @Value("\${app.security.rate-limit.max-attempts:5}") private val maxTentativas: Int,
    @Value("\${app.security.rate-limit.window-minutes:15}") private val janelaMinutos: Long,
) {

    private data class Estado(var falhas: Int = 0, var bloqueadoAte: Instant? = null)

    private val estados = ConcurrentHashMap<String, Estado>()

    fun verificar(identificador: String) {
        if (!habilitado) return
        val estado = estados[chave(identificador)] ?: return
        val bloqueadoAte = estado.bloqueadoAte ?: return
        if (bloqueadoAte.isAfter(Instant.now())) {
            val minutos = ((bloqueadoAte.epochSecond - Instant.now().epochSecond) / 60).coerceAtLeast(1)
            throw TooManyRequestsException(
                "Muitas tentativas de login. Tente novamente em $minutos minuto(s).",
            )
        }
        estados.remove(chave(identificador))
    }

    fun registrarFalha(identificador: String): Boolean {
        if (!habilitado) return false
        val chave = chave(identificador)
        var bloqueouAgora = false
        estados.compute(chave) { _, atual ->
            val estado = atual ?: Estado()
            if (estado.bloqueadoAte?.isBefore(Instant.now()) == true) {
                estado.falhas = 0
                estado.bloqueadoAte = null
            }
            estado.falhas += 1
            if (estado.falhas >= maxTentativas) {
                estado.bloqueadoAte = Instant.now().plusSeconds(janelaMinutos * 60)
                bloqueouAgora = true
            }
            estado
        }
        return bloqueouAgora
    }

    fun limpar(identificador: String) {
        if (!habilitado) return
        estados.remove(chave(identificador))
    }

    private fun chave(identificador: String): String {
        val ip = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
            ?.request?.let { request -> request.remoteAddr }
            ?: "desconhecido"
        return "$ip|${identificador.trim().lowercase()}"
    }
}
