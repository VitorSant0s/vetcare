package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.repository.CompartilhamentoProntuarioRepository
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.temporal.ChronoUnit

class CompartilhamentoIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var compartilhamentos: CompartilhamentoProntuarioRepository

    private fun codigoDe(animalId: Long, token: String): String {
        val resposta = mockMvc.perform(
            post("/api/animais/$animalId/compartilhamento").header("Authorization", bearer(token)),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return com.jayway.jsonpath.JsonPath.read(resposta, "$.codigo")
    }

    @Test
    fun `veterinario compartilha e outro veterinario acessa pelo codigo`() {
        val vetA = criarUsuario("veta@teste.com", Perfil.VETERINARIO, nome = "Vet A")
        criarUsuario("vetb@teste.com", Perfil.VETERINARIO, nome = "Vet B")
        val tutor = criarTutor()
        val animal = criarAnimal(tutor, "Thor")
        criarConsulta(animal, tutor, vetA)
        val tokenA = login("veta@teste.com")
        val tokenB = login("vetb@teste.com")

        val codigo = codigoDe(animal.id!!, tokenA)
        org.junit.jupiter.api.Assertions.assertEquals(8, codigo.length)

        mockMvc.perform(
            get("/api/prontuarios/compartilhados/$codigo").header("Authorization", bearer(tokenB)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.animal.nome").value("Thor"))

        mockMvc.perform(
            get("/api/prontuarios/compartilhados/CODIGO0").header("Authorization", bearer(tokenB)),
        ).andExpect(status().isNotFound)

        org.junit.jupiter.api.Assertions.assertTrue(
            auditorias.findAll().any { it.acao == "PRONTUARIO_COMPARTILHADO_ACESSADO" },
        )
    }

    @Test
    fun `novo compartilhamento invalida o codigo anterior e permite revogar`() {
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor, "Thor")
        criarConsulta(animal, tutor, vet)
        val token = login("vet@teste.com")
        val primeiro = codigoDe(animal.id!!, token)
        val segundo = codigoDe(animal.id!!, token)

        mockMvc.perform(
            get("/api/prontuarios/compartilhados/$primeiro").header("Authorization", bearer(token)),
        ).andExpect(status().isBadRequest)

        mockMvc.perform(
            get("/api/prontuarios/compartilhados/$segundo").header("Authorization", bearer(token)),
        ).andExpect(status().isOk)

        mockMvc.perform(
            delete("/api/animais/${animal.id}/compartilhamento").header("Authorization", bearer(token)),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/prontuarios/compartilhados/$segundo").header("Authorization", bearer(token)),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `codigo expirado nao permite acesso`() {
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor, "Thor")
        criarConsulta(animal, tutor, vet)
        val token = login("vet@teste.com")
        val codigo = codigoDe(animal.id!!, token)

        val compartilhamento = compartilhamentos.findByCodigoIgnoreCase(codigo).orElseThrow()
        compartilhamento.expiraEm = Instant.now().minus(1, ChronoUnit.DAYS)
        compartilhamentos.save(compartilhamento)

        mockMvc.perform(
            get("/api/prontuarios/compartilhados/$codigo").header("Authorization", bearer(token)),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `veterinario nao compartilha prontuario de paciente alheio`() {
        val vetA = criarUsuario("veta@teste.com", Perfil.VETERINARIO)
        criarUsuario("vetb@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor, "Thor")
        criarConsulta(animal, tutor, vetA)

        mockMvc.perform(
            post("/api/animais/${animal.id}/compartilhamento")
                .header("Authorization", bearer(login("vetb@teste.com"))),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `recepcionista nao compartilha nem busca prontuario`() {
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor, "Thor")
        criarConsulta(animal, tutor, vet)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/animais/${animal.id}/compartilhamento").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            get("/api/prontuarios/compartilhados/QUALQUER1").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)
    }
}
