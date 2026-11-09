package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VeterinarioIntegrationTest : IntegrationTestBase() {

    private val corpo = """
        {"nome":"Dra. Camila Andrade","email":"camila@vetcare.com","senha":"Senha@123",
         "crmv":"CRMV-BA 12345","especialidade":"Clínica de pequenos animais"}
    """.trimIndent()

    @Test
    fun `administrador cadastra e lista veterinarios`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            post("/api/veterinarios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.crmv").value("CRMV-BA 12345"))
            .andExpect(jsonPath("$.especialidade").value("Clínica de pequenos animais"))
            .andExpect(jsonPath("$.perfil").value("VETERINARIO"))

        mockMvc.perform(
            get("/api/veterinarios").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].nome").value("Dra. Camila Andrade"))
    }

    @Test
    fun `crmv e obrigatorio para cadastrar veterinario`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            post("/api/veterinarios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Sem Crmv","email":"semcrmv@vetcare.com","senha":"Senha@123","especialidade":"Geral"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `email duplicado de veterinario retorna 409`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        criarUsuario("camila@vetcare.com", Perfil.VETERINARIO)
        val token = login("admin@teste.com")

        mockMvc.perform(
            post("/api/veterinarios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isConflict)
    }

    @Test
    fun `recepcionista nao pode cadastrar veterinario`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/veterinarios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `administrador atualiza e desativa veterinario`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")
        val criado = mockMvc.perform(
            post("/api/veterinarios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val id = com.jayway.jsonpath.JsonPath.read<Int>(criado, "$.id")

        mockMvc.perform(
            put("/api/veterinarios/$id")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Dra. Camila Andrade","email":"camila@vetcare.com","crmv":"CRMV-BA 99999",
                     "especialidade":"Cardiologia","ativo":true}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.crmv").value("CRMV-BA 99999"))
            .andExpect(jsonPath("$.especialidade").value("Cardiologia"))

        mockMvc.perform(
            delete("/api/veterinarios/$id").header("Authorization", bearer(token)),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"camila@vetcare.com","senha":"Senha@123"}"""),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `veterinario nao gerencia o modulo de veterinarios`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        mockMvc.perform(
            get("/api/veterinarios").header("Authorization", bearer(token)),
        ).andExpect(status().isOk)

        mockMvc.perform(
            delete("/api/veterinarios/1").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)
    }
}
