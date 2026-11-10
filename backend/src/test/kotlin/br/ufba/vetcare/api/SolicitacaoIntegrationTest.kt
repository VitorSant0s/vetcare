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

class SolicitacaoIntegrationTest : IntegrationTestBase() {

    private fun corpo(animalId: Long, data: LocalDateTime): String =
        """{"animalId":$animalId,"dataPreferida":"$data","motivo":"Consulta de rotina"}"""

    @Test
    fun `cliente solicita e recepcao aprova gerando consulta`() {
        val tutor = definirSenhaTutor(criarTutor(cpf = "12345678909"))
        val animal = criarAnimal(tutor)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tokenCliente = loginCliente("12345678909")
        val tokenRecepcao = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0)

        val criada = mockMvc.perform(
            post("/api/cliente/solicitacoes")
                .header("Authorization", bearer(tokenCliente))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(animal.id!!, data)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("PENDENTE"))
            .andExpect(jsonPath("$.animalNome").value("Rex"))
            .andReturn().response.contentAsString
        val id = com.jayway.jsonpath.JsonPath.read<Int>(criada, "$.id")

        mockMvc.perform(
            get("/api/solicitacoes").param("status", "PENDENTE")
                .header("Authorization", bearer(tokenRecepcao)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))

        mockMvc.perform(
            post("/api/solicitacoes/$id/aprovar")
                .header("Authorization", bearer(tokenRecepcao))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"veterinarioId":${vet.id},"dataHora":"$data","observacao":"Confirmado"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APROVADA"))
            .andExpect(jsonPath("$.consultaId").isNumber)

        mockMvc.perform(
            get("/api/consultas")
                .param("inicio", data.toLocalDate().toString())
                .param("fim", data.toLocalDate().toString())
                .header("Authorization", bearer(tokenRecepcao)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].animalNome").value("Rex"))

        mockMvc.perform(
            get("/api/cliente/solicitacoes").header("Authorization", bearer(tokenCliente)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].status").value("APROVADA"))
    }

    @Test
    fun `recepcao recusa solicitacao com motivo`() {
        val tutor = definirSenhaTutor(criarTutor(cpf = "12345678909"))
        val animal = criarAnimal(tutor)
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tokenCliente = loginCliente("12345678909")
        val tokenRecepcao = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0)

        val criada = mockMvc.perform(
            post("/api/cliente/solicitacoes")
                .header("Authorization", bearer(tokenCliente))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(animal.id!!, data)),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val id = com.jayway.jsonpath.JsonPath.read<Int>(criada, "$.id")

        mockMvc.perform(
            post("/api/solicitacoes/$id/recusar")
                .header("Authorization", bearer(tokenRecepcao))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"motivo":"Horário indisponível"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("RECUSADA"))
            .andExpect(jsonPath("$.observacaoResposta").value("Horário indisponível"))
    }

    @Test
    fun `cliente nao solicita para pet de outro tutor`() {
        definirSenhaTutor(criarTutor("Tutor A", "12345678909"))
        val tutorB = criarTutor("Tutor B", "98765432100")
        val petB = criarAnimal(tutorB, "Bolt")
        val token = loginCliente("12345678909")

        mockMvc.perform(
            post("/api/cliente/solicitacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(petB.id!!, LocalDateTime.now().plusDays(1))),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `veterinario nao aprova solicitacao`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        mockMvc.perform(
            post("/api/solicitacoes/1/aprovar")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"veterinarioId":1,"dataHora":"2030-01-01T10:00:00"}"""),
        ).andExpect(status().isForbidden)
    }
}
