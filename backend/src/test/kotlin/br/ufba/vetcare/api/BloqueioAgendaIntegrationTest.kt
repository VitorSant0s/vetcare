package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.repository.NotificacaoRepository
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

class BloqueioAgendaIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var notificacoes: NotificacaoRepository

    private fun corpoConsulta(animalId: Long, tutorId: Long, vetId: Long, data: LocalDateTime) =
        """{"animalId":$animalId,"tutorId":$tutorId,"veterinarioId":$vetId,"dataHora":"$data"}"""

    @Test
    fun `veterinario bloqueia dia cancelando consultas e notificando tutores`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val dia = LocalDate.now().plusDays(10)
        val consulta = criarConsulta(animal, tutor, vet, dataHora = dia.atTime(10, 0))
        val tokenVet = login("vet@teste.com")
        val tokenRecepcao = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/veterinarios/me/bloqueios")
                .header("Authorization", bearer(tokenVet))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"data":"$dia","motivo":"Congresso de veterinária"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data").value(dia.toString()))
            .andExpect(jsonPath("$.consultasCanceladas").value(1))

        val atualizada = consultas.findById(consulta.id!!).orElseThrow()
        assertEquals(br.ufba.vetcare.domain.StatusConsulta.CANCELADA, atualizada.status)
        assertEquals("Agenda bloqueada: Congresso de veterinária", atualizada.motivoCancelamento)
        assertTrue(notificacoes.findAll().any { it.mensagem.contains("bloqueou o dia") })

        mockMvc.perform(
            get("/api/veterinarios/me/bloqueios").header("Authorization", bearer(tokenVet)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))

        // recepção não consegue agendar no dia bloqueado
        mockMvc.perform(
            post("/api/consultas")
                .header("Authorization", bearer(tokenRecepcao))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, dia.atTime(11, 0))),
        ).andExpect(status().isConflict)

        mockMvc.perform(
            delete("/api/veterinarios/me/bloqueios/$dia").header("Authorization", bearer(tokenVet)),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            post("/api/consultas")
                .header("Authorization", bearer(tokenRecepcao))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, dia.atTime(11, 0))),
        ).andExpect(status().isCreated)

        assertTrue(auditorias.findAll().any { it.acao == "AGENDA_BLOQUEADA" })
        assertTrue(auditorias.findAll().any { it.acao == "AGENDA_DESBLOQUEADA" })
        assertTrue(auditorias.findAll().any { it.acao == "CONSULTA_CANCELADA_POR_BLOQUEIO" })
    }

    @Test
    fun `nao permite bloquear data passada nem duplicada`() {
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")
        val ontem = LocalDate.now().minusDays(1)

        mockMvc.perform(
            post("/api/veterinarios/me/bloqueios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"data":"$ontem","motivo":"Teste"}"""),
        ).andExpect(status().isBadRequest)

        val amanha = LocalDate.now().plusDays(1)
        mockMvc.perform(
            post("/api/veterinarios/me/bloqueios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"data":"$amanha","motivo":"Compromisso"}"""),
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/veterinarios/me/bloqueios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"data":"$amanha","motivo":"Duplicado"}"""),
        ).andExpect(status().isConflict)
    }

    @Test
    fun `recepcao nao pode bloquear agenda do veterinario`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")
        val dia = LocalDate.now().plusDays(2)

        mockMvc.perform(
            post("/api/veterinarios/me/bloqueios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"data":"$dia","motivo":"Teste"}"""),
        ).andExpect(status().isForbidden)
    }
}
