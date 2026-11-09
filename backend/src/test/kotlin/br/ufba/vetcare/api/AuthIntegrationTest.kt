package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.hamcrest.Matchers.hasKey
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthIntegrationTest : IntegrationTestBase() {

    @Test
    fun `login com credenciais validas retorna token e perfil`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR, nome = "Admin Teste")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"admin@teste.com","senha":"Senha@123"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.usuario.perfil").value("ADMINISTRADOR"))
            .andExpect(jsonPath("$.usuario.senha").doesNotExist())
    }

    @Test
    fun `login com senha invalida retorna 401`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"admin@teste.com","senha":"errada"}"""),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.erro").value("Não autenticado"))
    }

    @Test
    fun `usuario inativo nao autentica`() {
        criarUsuario("inativo@teste.com", Perfil.RECEPCIONISTA, ativo = false)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"inativo@teste.com","senha":"Senha@123"}"""),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `endpoint protegido sem token retorna 401`() {
        mockMvc.perform(get("/api/tutores"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$", hasKey("mensagem")))
    }
}
