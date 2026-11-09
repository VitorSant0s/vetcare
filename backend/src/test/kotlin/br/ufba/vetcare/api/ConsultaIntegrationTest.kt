package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConsultaIntegrationTest : IntegrationTestBase() {

    private val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @Test
    fun `recepcionista agenda consulta e ela aparece na agenda`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO, nome = "Dra. Camila")
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0)

        mockMvc.perform(
            post("/api/consultas")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"animalId":${animal.id},"tutorId":${tutor.id},"veterinarioId":${vet.id},"dataHora":"${data.format(formato)}"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("AGENDADA"))
            .andExpect(jsonPath("$.veterinarioNome").value("Dra. Camila"))

        mockMvc.perform(
            get("/api/consultas")
                .param("inicio", data.toLocalDate().toString())
                .param("fim", data.toLocalDate().toString())
                .header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].animalNome").value("Rex"))
    }

    @Test
    fun `conflito de horario do veterinario retorna 409`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(3).withHour(14).withMinute(30).withSecond(0).withNano(0)
        val corpo =
            """{"animalId":${animal.id},"tutorId":${tutor.id},"veterinarioId":${vet.id},"dataHora":"${data.format(formato)}"}"""

        mockMvc.perform(
            post("/api/consultas").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(corpo),
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/consultas").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(corpo),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.mensagem").value("O veterinário já possui uma consulta neste horário."))
    }

    @Test
    fun `animal de outro tutor retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutorA = criarTutor("Tutor A", "11122233344")
        val tutorB = criarTutor("Tutor B", "55566677788")
        val animalDeA = criarAnimal(tutorA)
        val token = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(11)

        mockMvc.perform(
            post("/api/consultas")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"animalId":${animalDeA.id},"tutorId":${tutorB.id},"veterinarioId":${vet.id},"dataHora":"${data.format(formato)}"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value("O animal selecionado não pertence ao tutor informado."))
    }

    @Test
    fun `veterinario nao pode agendar consulta`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        mockMvc.perform(
            post("/api/consultas")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"animalId":1,"tutorId":1,"veterinarioId":1,"dataHora":"${LocalDateTime.now().plusDays(1).format(formato)}"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `consulta no passado retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/consultas")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"animalId":${animal.id},"tutorId":${tutor.id},"veterinarioId":${vet.id},"dataHora":"2020-01-01T10:00:00"}
                    """.trimIndent(),
                ),
        ).andExpect(status().isBadRequest)
    }
}
