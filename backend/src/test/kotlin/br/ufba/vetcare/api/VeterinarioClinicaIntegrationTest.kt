package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VeterinarioClinicaIntegrationTest : IntegrationTestBase() {

    @Test
    fun `veterinario ve apenas os proprios pacientes`() {
        val vetA = criarUsuario("veta@teste.com", Perfil.VETERINARIO, nome = "Vet A")
        val vetB = criarUsuario("vetb@teste.com", Perfil.VETERINARIO, nome = "Vet B")
        val tutor = criarTutor()
        val animalA = criarAnimal(tutor, "Thor")
        val animalB = criarAnimal(tutor, "Luna")
        criarConsulta(animalA, tutor, vetA)
        criarConsulta(animalB, tutor, vetB)
        val token = login("veta@teste.com")

        mockMvc.perform(
            get("/api/veterinarios/me/pacientes").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].animal.nome").value("Thor"))
            .andExpect(jsonPath("$[0].totalConsultas").value(1))

        mockMvc.perform(
            get("/api/animais").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].nome").value("Thor"))
    }

    @Test
    fun `veterinario nao acessa prontuario de paciente alheio`() {
        val vetA = criarUsuario("veta@teste.com", Perfil.VETERINARIO, nome = "Vet A")
        val vetB = criarUsuario("vetb@teste.com", Perfil.VETERINARIO, nome = "Vet B")
        val tutor = criarTutor()
        val animalA = criarAnimal(tutor, "Thor")
        val animalB = criarAnimal(tutor, "Luna")
        criarConsulta(animalA, tutor, vetA)
        criarConsulta(animalB, tutor, vetB)
        val token = login("veta@teste.com")

        mockMvc.perform(
            get("/api/animais/${animalA.id}/prontuario").header("Authorization", bearer(token)),
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/animais/${animalB.id}/prontuario").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            get("/api/animais/${animalB.id}").header("Authorization", bearer(token)),
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `veterinario so vacina os proprios pacientes`() {
        val vetA = criarUsuario("veta@teste.com", Perfil.VETERINARIO, nome = "Vet A")
        val vetB = criarUsuario("vetb@teste.com", Perfil.VETERINARIO, nome = "Vet B")
        val tutor = criarTutor()
        val animalA = criarAnimal(tutor, "Thor")
        val animalB = criarAnimal(tutor, "Luna")
        criarConsulta(animalA, tutor, vetA)
        criarConsulta(animalB, tutor, vetB)
        val token = login("veta@teste.com")
        val corpo = """
            {"tipoVacina":"V10","dataAplicacao":"2026-01-10","proximaDose":"2027-01-10"}
        """.trimIndent()

        mockMvc.perform(
            post("/api/animais/${animalA.id}/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/animais/${animalB.id}/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `administrador continua vendo todos os animais`() {
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val tutor = criarTutor()
        criarAnimal(tutor, "Thor")
        criarAnimal(tutor, "Luna")
        criarConsulta(criarAnimal(tutor, "Bolt"), tutor, vet)

        mockMvc.perform(
            get("/api/animais").header("Authorization", bearer(login("admin@teste.com"))),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(3))
    }
}
