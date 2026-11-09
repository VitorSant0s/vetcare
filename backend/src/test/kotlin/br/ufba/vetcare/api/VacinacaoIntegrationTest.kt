package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VacinacaoIntegrationTest : IntegrationTestBase() {

    private fun corpo(tipo: String = "V10 - Polivalente", proxima: String = "2027-01-10") = """
        {"tipoVacina":"$tipo","lote":"LOTE-2026-A","dataAplicacao":"2026-01-10","proximaDose":"$proxima","observacoes":"Primeira dose"}
    """.trimIndent()

    @Test
    fun `recepcionista registra vacinacao e prontuario exibe proxima dose`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/animais/${animal.id}/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo()),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.tipoVacina").value("V10 - Polivalente"))
            .andExpect(jsonPath("$.proximaDose").value("2027-01-10"))
            .andExpect(jsonPath("$.veterinarioNome").doesNotExist())

        mockMvc.perform(
            get("/api/animais/${animal.id}/vacinacoes").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))

        mockMvc.perform(
            get("/api/animais/${animal.id}/prontuario").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.vacinacoes.length()").value(1))
            .andExpect(jsonPath("$.vacinacoes[0].proximaDose").value("2027-01-10"))
    }

    @Test
    fun `veterinario assina a vacinacao`() {
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO, nome = "Dra. Camila")
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("vet@teste.com")

        mockMvc.perform(
            post("/api/animais/${animal.id}/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo()),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.veterinarioNome").value("Dra. Camila"))
            .andExpect(jsonPath("$.veterinarioId").value(vet.id))
    }

    @Test
    fun `data de aplicacao futura retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/animais/${animal.id}/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo().replace("2026-01-10", "2030-01-10")),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `proxima dose anterior a aplicacao retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/animais/${animal.id}/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(proxima = "2025-12-01")),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `veterinario exclui vacinacao mas recepcionista nao`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val tokenRecepcao = login("recepcao@teste.com")
        val criada = mockMvc.perform(
            post("/api/animais/${animal.id}/vacinacoes")
                .header("Authorization", bearer(tokenRecepcao))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo()),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val id = com.jayway.jsonpath.JsonPath.read<Int>(criada, "$.id")

        mockMvc.perform(
            delete("/api/vacinacoes/$id").header("Authorization", bearer(tokenRecepcao)),
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/api/vacinacoes/$id").header("Authorization", bearer(login("vet@teste.com"))),
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `vacinacao para animal inexistente retorna 404`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/animais/9999/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo()),
        ).andExpect(status().isNotFound)
    }
}
