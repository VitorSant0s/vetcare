package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuditoriaIntegrationTest : IntegrationTestBase() {

    @Test
    fun `administrador consulta a trilha de auditoria`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            get("/api/auditoria").param("acao", "LOGIN_SUCESSO")
                .header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].acao").value("LOGIN_SUCESSO"))

        assertTrue(auditorias.findAll().any { it.acao == "AUDITORIA_CONSULTADA" })
    }

    @Test
    fun `apenas administrador acessa a auditoria`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)

        mockMvc.perform(
            get("/api/auditoria").header("Authorization", bearer(login("recepcao@teste.com"))),
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            get("/api/auditoria").header("Authorization", bearer(login("vet@teste.com"))),
        ).andExpect(status().isForbidden)
    }
}
