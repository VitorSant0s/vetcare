package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class PagamentoIntegrationTest : IntegrationTestBase() {

    private fun corpo(tutorId: Long, consultaId: Long? = null, valor: String = "180.00"): String {
        val consulta = consultaId?.let { "\"consultaId\":$it," } ?: ""
        return """
            {$consulta"tutorId":$tutorId,"valor":$valor,"formaPagamento":"PIX",
             "dataPagamento":"${LocalDate.now()}","observacoes":"Consulta e medicação"}
        """.trimIndent()
    }

    @Test
    fun `recepcionista registra pagamento e dashboard consolida`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val consulta = criarConsulta(animal, tutor, vet, status = br.ufba.vetcare.domain.StatusConsulta.REALIZADA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/pagamentos")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(tutor.id!!, consulta.id!!)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.valor").value(180.00))
            .andExpect(jsonPath("$.formaPagamento").value("PIX"))
            .andExpect(jsonPath("$.consultaId").value(consulta.id))

        mockMvc.perform(
            get("/api/pagamentos").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))

        mockMvc.perform(
            get("/api/financeiro/dashboard").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalRecebido").value(180.00))
            .andExpect(jsonPath("$.quantidadePagamentos").value(1))
            .andExpect(jsonPath("$.porForma[0].forma").value("PIX"))
    }

    @Test
    fun `consulta de outro tutor retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutorA = criarTutor("Tutor A", "11144477735")
        val tutorB = criarTutor("Tutor B", "39053344705")
        val animal = criarAnimal(tutorA)
        val consulta = criarConsulta(animal, tutorA, vet)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/pagamentos")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(tutorB.id!!, consulta.id!!)),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `data futura e valor zero sao rejeitados`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/pagamentos")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"tutorId":${tutor.id},"valor":100.00,"formaPagamento":"DINHEIRO","dataPagamento":"2030-01-01"}""",
                ),
        ).andExpect(status().isBadRequest)

        mockMvc.perform(
            post("/api/pagamentos")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"tutorId":${tutor.id},"valor":0,"formaPagamento":"DINHEIRO","dataPagamento":"${LocalDate.now()}"}""",
                ),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `veterinario nao acessa financeiro`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        mockMvc.perform(
            get("/api/financeiro/dashboard").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)
    }
}
