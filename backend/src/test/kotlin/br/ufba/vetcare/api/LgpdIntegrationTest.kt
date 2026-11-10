package br.ufba.vetcare.api

import br.ufba.vetcare.domain.repository.TutorRepository
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LgpdIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var tutorRepository: TutorRepository

    @Test
    fun `cliente registra consentimento e exporta os proprios dados`() {
        val tutor = definirSenhaTutor(criarTutor(cpf = "12345678909"))
        criarAnimal(tutor, "Thor")
        val token = loginCliente("12345678909")

        mockMvc.perform(
            post("/api/cliente/auth/consentimento")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"versao":"1.0"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.consentimentoVersao").value("1.0"))
            .andExpect(jsonPath("$.consentimentoEm").isNotEmpty)

        mockMvc.perform(
            get("/api/cliente/meus-dados").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tutor.nome").value(tutor.nome))
            .andExpect(jsonPath("$.tutor.senha").doesNotExist())
            .andExpect(jsonPath("$.pets.length()").value(1))

        assertTrue(auditorias.findAll().any { it.acao == "CONSENTIMENTO_LGPD_REGISTRADO" })
        assertTrue(auditorias.findAll().any { it.acao == "LGPD_DADOS_EXPORTADOS" })
    }

    @Test
    fun `cliente anonimiza a conta e perde o acesso`() {
        val tutor = definirSenhaTutor(criarTutor(cpf = "12345678909"))
        val token = loginCliente("12345678909")

        mockMvc.perform(
            delete("/api/cliente/conta").header("Authorization", bearer(token)),
        ).andExpect(status().isNoContent)

        val anonimizado = tutorRepository.findById(tutor.id!!).orElseThrow()
        assertTrue(anonimizado.anonimizado)
        assertFalse(anonimizado.ativo)
        assertTrue(anonimizado.email.startsWith("anonimo+"))
        assertTrue(anonimizado.senha == null)

        mockMvc.perform(
            post("/api/cliente/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"cpf":"12345678909","senha":"Cliente@123"}"""),
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            get("/api/cliente/pets").header("Authorization", bearer(token)),
        ).andExpect(status().isUnauthorized)

        assertTrue(auditorias.findAll().any { it.acao == "LGPD_EXCLUSAO_SOLICITADA" })
    }
}
