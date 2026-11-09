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
                    {"nome":"Ana Souza","cpf":"123.456.789-09","email":"ana@teste.com","telefone":"(71) 98888-1111","endereco":"Rua A, 10"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.cpf").value("12345678909"))
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

    @Test
    fun `auditoria registra o email de quem fez a mutacao`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/tutores")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Ana Souza","cpf":"12345678909","email":"ana@teste.com","telefone":"(71) 98888-1111"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isCreated)

        val registro = auditorias.findAll().first { it.acao == "TUTOR_CRIADO" }
        org.junit.jupiter.api.Assertions.assertEquals("recepcao@teste.com", registro.usuarioEmail)
        org.junit.jupiter.api.Assertions.assertFalse(registro.usuarioEmail!!.contains("UsuarioAutenticado"))
    }

    @Test
    fun `cpf sem digitos validos retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/tutores")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Cpf Invalido","cpf":"abcdefghijk","email":"cpf@teste.com","telefone":"(71) 90000-0000"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `atualiza tutor e animal`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/tutores/${tutor.id}")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Tutor Atualizado","cpf":"11144477735","email":"novo@teste.com","telefone":"(71) 91111-2222"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Tutor Atualizado"))

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/animais/${animal.id}")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Rex Atualizado","especie":"Cão","raca":"Beagle","tutorId":${tutor.id}}
                    """.trimIndent(),
                ),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Rex Atualizado"))
    }

    @Test
    fun `animal com consulta futura nao pode ser excluido`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")
        agendarConsulta(token, animal.id!!, tutor.id!!, vet.id!!)

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/animais/${animal.id}")
                .header("Authorization", bearer(token)),
        ).andExpect(status().isConflict)
    }

    @Test
    fun `animal com consulta futura nao pode ser transferido`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutorA = criarTutor("Tutor A", "11144477735")
        val tutorB = criarTutor("Tutor B", "39053344705")
        val animal = criarAnimal(tutorA)
        val token = login("recepcao@teste.com")
        agendarConsulta(token, animal.id!!, tutorA.id!!, vet.id!!)

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/animais/${animal.id}")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Rex","especie":"Cão","tutorId":${tutorB.id}}
                    """.trimIndent(),
                ),
        ).andExpect(status().isConflict)
    }

    private fun agendarConsulta(token: String, animalId: Long, tutorId: Long, veterinarioId: Long) {
        val data = java.time.LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0)
        mockMvc.perform(
            post("/api/consultas")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"animalId":$animalId,"tutorId":$tutorId,"veterinarioId":$veterinarioId,"dataHora":"$data"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isCreated)
    }
}
