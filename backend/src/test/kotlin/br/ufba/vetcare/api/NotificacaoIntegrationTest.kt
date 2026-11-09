package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Vacinacao
import br.ufba.vetcare.domain.repository.VacinacaoRepository
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

class NotificacaoIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var vacinacaoRepository: VacinacaoRepository

    @Test
    fun `processa avisos de consulta e vacina e nao duplica`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        criarConsulta(
            animal,
            tutor,
            vet,
            dataHora = LocalDateTime.now().plusHours(12),
            status = br.ufba.vetcare.domain.StatusConsulta.AGENDADA,
        )
        vacinacaoRepository.save(
            Vacinacao(
                animal = animal,
                veterinario = vet,
                tipoVacina = "V8",
                dataAplicacao = LocalDate.now().minusMonths(11),
                proximaDose = LocalDate.now().plusDays(5),
            ),
        )
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/notificacoes/processar").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.geradas").value(2))
            .andExpect(jsonPath("$.enviadas").value(2))

        mockMvc.perform(
            get("/api/notificacoes").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].status").value("ENVIADA"))
            .andExpect(jsonPath("$[0].canal").value("EMAIL"))

        mockMvc.perform(
            post("/api/notificacoes/processar").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.geradas").value(0))
            .andExpect(jsonPath("$.enviadas").value(0))
    }

    @Test
    fun `veterinario nao acessa notificacoes`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        mockMvc.perform(
            get("/api/notificacoes").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)
    }
}
