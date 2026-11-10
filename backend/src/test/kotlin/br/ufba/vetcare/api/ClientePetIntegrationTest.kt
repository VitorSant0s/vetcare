package br.ufba.vetcare.api

import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ClientePetIntegrationTest : IntegrationTestBase() {

    @Test
    fun `cliente lista cadastra e edita os proprios pets`() {
        val tutor = definirSenhaTutor(criarTutor(cpf = "12345678909"))
        criarAnimal(tutor, "Thor")
        val token = loginCliente("12345678909")

        mockMvc.perform(
            get("/api/cliente/pets").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("Thor"))

        val criado = mockMvc.perform(
            post("/api/cliente/pets")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"nome":"Luna","especie":"Gato","raca":"Siamês","sexo":"Fêmea","dataNascimento":"2023-05-10"}""",
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.tutorId").value(tutor.id))
            .andExpect(jsonPath("$.tutorNome").value(tutor.nome))
            .andReturn().response.contentAsString
        val animalId = com.jayway.jsonpath.JsonPath.read<Int>(criado, "$.id")

        mockMvc.perform(
            put("/api/cliente/pets/$animalId")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"nome":"Luna Maria","especie":"Gato","raca":"Siamês","sexo":"Fêmea"}""",
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Luna Maria"))
    }

    @Test
    fun `cliente nao acessa pet de outro tutor`() {
        val tutorA = definirSenhaTutor(criarTutor("Tutor A", "12345678909"))
        val tutorB = criarTutor("Tutor B", "98765432100")
        val petB = criarAnimal(tutorB, "Bolt")
        val token = loginCliente("12345678909")
        org.junit.jupiter.api.Assertions.assertNotEquals(tutorA.id, tutorB.id)

        mockMvc.perform(
            put("/api/cliente/pets/${petB.id}")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Invasão","especie":"Cão"}"""),
        ).andExpect(status().isNotFound)
    }
}
