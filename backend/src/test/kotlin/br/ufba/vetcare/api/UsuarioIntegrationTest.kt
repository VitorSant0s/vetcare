package br.ufba.vetcare.api

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UsuarioIntegrationTest : IntegrationTestBase() {

    @Test
    fun `administrador cria e lista usuarios`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            post("/api/usuarios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Novo Veterinário","email":"novo.vet@teste.com","senha":"Senha@123","perfil":"VETERINARIO","ativo":true}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("novo.vet@teste.com"))
            .andExpect(jsonPath("$.perfil").value("VETERINARIO"))

        mockMvc.perform(
            get("/api/usuarios").header("Authorization", bearer(token)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    fun `email duplicado retorna 409`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        criarUsuario("existente@teste.com", Perfil.RECEPCIONISTA)
        val token = login("admin@teste.com")

        mockMvc.perform(
            post("/api/usuarios")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Duplicado","email":"existente@teste.com","senha":"Senha@123","perfil":"RECEPCIONISTA","ativo":true}
                    """.trimIndent(),
                ),
        ).andExpect(status().isConflict)
    }

    @Test
    fun `recepcionista nao pode gerenciar usuarios`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            get("/api/usuarios").header("Authorization", bearer(token)),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `exclusao e logica e impede novo login`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val alvo = criarUsuario("alvo@teste.com", Perfil.RECEPCIONISTA)
        val token = login("admin@teste.com")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/usuarios/${alvo.id}")
                .header("Authorization", bearer(token)),
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"alvo@teste.com","senha":"Senha@123"}"""),
        ).andExpect(status().isUnauthorized)

        val noBanco = usuarios.findById(alvo.id!!).orElseThrow()
        org.junit.jupiter.api.Assertions.assertFalse(noBanco.ativo)
        org.junit.jupiter.api.Assertions.assertTrue(
            auditorias.findAll().any { it.acao == "USUARIO_EXCLUIDO" && it.entidadeId == alvo.id },
        )
    }

    @Test
    fun `administrador nao pode se desativar`() {
        val admin = criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/usuarios/${admin.id}")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Admin Teste","email":"admin@teste.com","perfil":"ADMINISTRADOR","ativo":false}
                    """.trimIndent(),
                ),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `administrador nao pode se rebaixar`() {
        val admin = criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/usuarios/${admin.id}")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Admin Teste","email":"admin@teste.com","perfil":"RECEPCIONISTA","ativo":true}
                    """.trimIndent(),
                ),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `administrador pode desativar outro administrador`() {
        criarUsuario("admin@teste.com", Perfil.ADMINISTRADOR)
        val outro = criarUsuario("outro.admin@teste.com", Perfil.ADMINISTRADOR)
        val token = login("admin@teste.com")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/usuarios/${outro.id}")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Outro Admin","email":"outro.admin@teste.com","perfil":"ADMINISTRADOR","ativo":false}
                    """.trimIndent(),
                ),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.ativo").value(false))
    }
}
