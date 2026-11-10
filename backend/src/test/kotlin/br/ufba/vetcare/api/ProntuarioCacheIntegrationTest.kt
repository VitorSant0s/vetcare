package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ProntuarioCacheIntegrationTest : IntegrationTestBase() {

    @Test
    fun `prontuario usa cache mas audita todo acesso e invalida em mutacao`() {
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor, "Thor")
        criarConsulta(animal, tutor, vet)
        val token = login("vet@teste.com")

        fun abrirProntuario() {
            mockMvc.perform(
                get("/api/animais/${animal.id}/prontuario").header("Authorization", bearer(token)),
            ).andExpect(status().isOk)
        }

        abrirProntuario()
        val cache = cacheManager.getCache("prontuario")!!
        assertTrue(
            (cache as org.springframework.cache.concurrent.ConcurrentMapCache).nativeCache.isNotEmpty(),
        )

        val acessosAntes = auditorias.findAll().count { it.acao == "PRONTUARIO_ACESSADO" }
        abrirProntuario()
        val acessosDepois = auditorias.findAll().count { it.acao == "PRONTUARIO_ACESSADO" }
        assertTrue(acessosDepois > acessosAntes, "cache hit não pode pular a auditoria")

        mockMvc.perform(
            post("/api/animais/${animal.id}/vacinacoes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"tipoVacina":"V10","dataAplicacao":"2026-01-10"}"""),
        ).andExpect(status().isCreated)

        assertTrue(
            (cache as org.springframework.cache.concurrent.ConcurrentMapCache).nativeCache.isEmpty(),
            "vacinação deve invalidar o cache do prontuário",
        )
    }
}
