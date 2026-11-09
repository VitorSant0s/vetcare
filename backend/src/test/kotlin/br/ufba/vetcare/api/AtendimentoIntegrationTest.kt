package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AtendimentoIntegrationTest : IntegrationTestBase() {

    private val corpo = """
        {
          "anamnese":"Animal apresenta apatia e falta de apetite há dois dias.",
          "diagnostico":"Gastrite leve",
          "procedimentos":"Exame físico e hemograma",
          "observacoes":"Retorno em 7 dias se não houver melhora.",
          "prescricoes":[
            {"medicamento":"Omeprazol 10mg","dosagem":"1 comprimido","frequencia":"1x ao dia","duracao":"5 dias","orientacoes":"Administrar em jejum"}
          ]
        }
    """.trimIndent()

    @Test
    fun `veterinario registra atendimento e prontuario consolida historico`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO, nome = "Dra. Camila")
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val consulta = criarConsulta(animal, tutor, vet)
        val token = login("vet@teste.com")

        mockMvc.perform(
            post("/api/consultas/${consulta.id}/atendimento")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.diagnostico").value("Gastrite leve"))
            .andExpect(jsonPath("$.animalNome").value("Rex"))
            .andExpect(jsonPath("$.prescricoes.length()").value(1))
            .andExpect(jsonPath("$.prescricoes[0].medicamento").value("Omeprazol 10mg"))

        val consultaAtualizada = consultas.findById(consulta.id!!).orElseThrow()
        assertEquals(br.ufba.vetcare.domain.StatusConsulta.REALIZADA, consultaAtualizada.status)

        mockMvc.perform(
            get("/api/animais/${animal.id}/prontuario")
                .header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.animal.nome").value("Rex"))
            .andExpect(jsonPath("$.atendimentos.length()").value(1))
            .andExpect(jsonPath("$.atendimentos[0].diagnostico").value("Gastrite leve"))
            .andExpect(jsonPath("$.atendimentos[0].prescricoes[0].medicamento").value("Omeprazol 10mg"))
            .andExpect(jsonPath("$.consultas[0].status").value("REALIZADA"))

        assertTrue(auditorias.findAll().any { it.acao == "ATENDIMENTO_REGISTRADO" })
        assertTrue(auditorias.findAll().any { it.acao == "PRONTUARIO_ACESSADO" })
    }

    @Test
    fun `consulta nao pode receber dois atendimentos`() {
        crearDados()
        val consulta = consultas.findAll().first()
        val token = login("vet@teste.com")

        mockMvc.perform(
            post("/api/consultas/${consulta.id}/atendimento")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/consultas/${consulta.id}/atendimento")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isConflict)
    }

    @Test
    fun `outro veterinario nao registra atendimento alheio`() {
        crearDados()
        val consulta = consultas.findAll().first()
        criarUsuario("outro.vet@teste.com", Perfil.VETERINARIO, nome = "Dr. Outro")
        val token = login("outro.vet@teste.com")

        mockMvc.perform(
            post("/api/consultas/${consulta.id}/atendimento")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `recepcionista nao registra atendimento`() {
        crearDados()
        val consulta = consultas.findAll().first()
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/consultas/${consulta.id}/atendimento")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `consulta futura nao aceita atendimento`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val vet = usuarios.findByEmailIgnoreCase("vet@teste.com").orElseThrow()
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val consulta = criarConsulta(
            animal,
            tutor,
            vet,
            dataHora = java.time.LocalDateTime.now().plusDays(2),
        )
        val token = login("vet@teste.com")

        mockMvc.perform(
            post("/api/consultas/${consulta.id}/atendimento")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `prontuario de animal inexistente retorna 404`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        mockMvc.perform(
            get("/api/animais/9999/prontuario").header("Authorization", bearer(token)),
        ).andExpect(status().isNotFound)
    }

    private fun crearDados() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        criarConsulta(animal, tutor, vet)
    }
}
