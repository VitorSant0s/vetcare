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

class RelatorioIntegrationTest : IntegrationTestBase() {

    @Test
    fun `resumo consolidado traz consultas animais vacinas e financeiro`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        criarConsulta(animal, tutor, vet, status = br.ufba.vetcare.domain.StatusConsulta.REALIZADA)
        val token = login("admin@teste.com")

        mockMvc.perform(
            post("/api/pagamentos")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"tutorId":${tutor.id},"valor":250.00,"formaPagamento":"CARTAO_CREDITO","dataPagamento":"${LocalDate.now()}"}""",
                ),
        ).andExpect(status().isCreated)

        mockMvc.perform(
            get("/api/relatorios/resumo")
                .param("inicio", LocalDate.now().toString())
                .param("fim", LocalDate.now().toString())
                .header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.consultasTotal").value(1))
            .andExpect(jsonPath("$.consultasRealizadas").value(1))
            .andExpect(jsonPath("$.animaisCadastrados").value(1))
            .andExpect(jsonPath("$.animaisNovos").value(1))
            .andExpect(jsonPath("$.totalRecebido").value(250.00))
    }

    @Test
    fun `recepcionista acessa relatorios e veterinario nao`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)

        mockMvc.perform(
            get("/api/relatorios/resumo").header("Authorization", bearer(login("recepcao@teste.com"))),
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/relatorios/resumo").header("Authorization", bearer(login("vet@teste.com"))),
        ).andExpect(status().isForbidden)
    }
}
