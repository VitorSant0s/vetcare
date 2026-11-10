package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SegurancaIntegrationTest : IntegrationTestBase() {

    @Test
    fun `respostas possuem cabecalhos de seguranca`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andExpect(header().string("Permissions-Policy", "geolocation=(), microphone=(), camera=()"))
    }

    @Test
    fun `logout da equipe revoga tokens anteriores`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            get("/api/tutores").header("Authorization", bearer(token)),
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/auth/logout").header("Authorization", bearer(token)),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/tutores").header("Authorization", bearer(token)),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `logout do cliente revoga token anterior`() {
        definirSenhaTutor(criarTutor(cpf = "12345678909"))
        val token = loginCliente("12345678909")

        mockMvc.perform(
            get("/api/cliente/pets").header("Authorization", bearer(token)),
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/cliente/auth/logout").header("Authorization", bearer(token)),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/cliente/pets").header("Authorization", bearer(token)),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `token de cliente nao acessa api interna`() {
        definirSenhaTutor(criarTutor(cpf = "12345678909"))
        val token = loginCliente("12345678909")

        mockMvc.perform(
            post("/api/tutores")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"X","cpf":"12345678909","email":"x@x.com","telefone":"(71) 90000-0000"}"""),
        ).andExpect(status().isForbidden)
    }
}
