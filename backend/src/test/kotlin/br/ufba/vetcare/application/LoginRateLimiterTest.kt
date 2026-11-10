package br.ufba.vetcare.application

import br.ufba.vetcare.application.exception.TooManyRequestsException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoginRateLimiterTest {

    @Test
    fun `bloqueia apos o limite e libera ao limpar`() {
        val limiter = LoginRateLimiter(habilitado = true, maxTentativas = 3, janelaMinutos = 15)

        assertFalse(limiter.registrarFalha("admin@teste.com"))
        assertFalse(limiter.registrarFalha("admin@teste.com"))
        assertTrue(limiter.registrarFalha("admin@teste.com"))

        assertThrows(TooManyRequestsException::class.java) {
            limiter.verificar("admin@teste.com")
        }

        limiter.limpar("admin@teste.com")
        limiter.verificar("admin@teste.com")
    }

    @Test
    fun `desabilitado nao bloqueia`() {
        val limiter = LoginRateLimiter(habilitado = false, maxTentativas = 1, janelaMinutos = 15)
        repeat(5) { limiter.registrarFalha("x") }
        limiter.verificar("x")
    }
}
