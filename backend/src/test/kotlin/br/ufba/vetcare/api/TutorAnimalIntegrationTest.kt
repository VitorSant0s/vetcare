package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TutorAnimalIntegrationTest : IntegrationTestBase() {

    @Test
    fun `recepcionista cadastra tutor e animal e vincula corretamente`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        val respostaTutor = mockMvc.perform(
            post("/api/tutores")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Ana Souza","cpf":"123.456.789-01","email":"ana@teste.com","telefone":"(71) 98888-1111","endereco":"Rua A, 10"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.cpf").value("12345678901"))
            .andReturn().response.contentAsString

        val tutorId = com.jayway.jsonpath.JsonPath.read<Int>(respostaTutor, "$.id")

        mockMvc.perform(
            post("/api/animais")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Thor","especie":"Cão","raca":"Golden","sexo":"Macho","dataNascimento":"2022-03-14","tutorId":$tutorId}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.tutorNome").value("Ana Souza"))
            .andExpect(jsonPath("$.tutorId").value(tutorId))
    }

    @Test
    fun `animal com tutor inexistente retorna 404`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/animais")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Rex","especie":"Cão","tutorId":9999}
                    """.trimIndent(),
                ),
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `tutor com animais nao pode ser excluido`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        criarAnimal(tutor)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/tutores/${tutor.id}")
                .header("Authorization", bearer(token)),
        ).andExpect(status().isConflict)
    }

    @Test
    fun `veterinario consulta tutores mas nao pode cadastrar`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/tutores").header("Authorization", bearer(token)),
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/tutores")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Bloqueado","cpf":"00011122233","email":"b@teste.com","telefone":"(71) 90000-0000"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isForbidden)
    }
}
