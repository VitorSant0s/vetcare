package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ClienteAuthIntegrationTest : IntegrationTestBase() {

    @Test
    fun `cliente autentica com cpf formatado e troca a senha`() {
        val tutor = definirSenhaTutor(criarTutor(cpf = "12345678909"))

        mockMvc.perform(
            post("/api/cliente/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"cpf":"123.456.789-09","senha":"Cliente@123"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.tutor.nome").value(tutor.nome))
            .andExpect(jsonPath("$.primeiroAcesso").value(false))

        val token = loginCliente("12345678909")
        mockMvc.perform(
            get("/api/cliente/auth/me").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.cpf").value("12345678909"))

        mockMvc.perform(
            put("/api/cliente/auth/senha")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"senhaAtual":"errada","novaSenha":"Nova@123"}"""),
        ).andExpect(status().isBadRequest)

        mockMvc.perform(
            put("/api/cliente/auth/senha")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"senhaAtual":"Cliente@123","novaSenha":"Nova@123"}"""),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            post("/api/cliente/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"cpf":"12345678909","senha":"Cliente@123"}"""),
        ).andExpect(status().isUnauthorized)

        loginCliente("12345678909", "Nova@123")
    }

    @Test
    fun `cliente inativo nao autentica`() {
        val tutor = criarTutor(cpf = "12345678909")
        tutor.ativo = false
        definirSenhaTutor(tutor)

        mockMvc.perform(
            post("/api/cliente/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"cpf":"12345678909","senha":"Cliente@123"}"""),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `tutor sem senha nao acessa o portal`() {
        criarTutor(cpf = "12345678909")

        mockMvc.perform(
            post("/api/cliente/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"cpf":"12345678909","senha":"Cliente@123"}"""),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `token de cliente nao acessa area administrativa`() {
        definirSenhaTutor(criarTutor(cpf = "12345678909"))
        val token = loginCliente("12345678909")

        mockMvc.perform(
            get("/api/usuarios").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)

        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val tokenAdmin = login("admin@teste.com")
        mockMvc.perform(
            get("/api/cliente/pets").header("Authorization", bearer(tokenAdmin)),
        ).andExpect(status().isForbidden)
    }
}
