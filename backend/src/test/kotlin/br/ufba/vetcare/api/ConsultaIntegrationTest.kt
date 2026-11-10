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

    private fun corpoConsulta(
        animalId: Long,
        tutorId: Long,
        veterinarioId: Long,
        data: LocalDateTime,
    ) = """{"animalId":$animalId,"tutorId":$tutorId,"veterinarioId":$veterinarioId,"dataHora":"${data.format(formato)}"}"""

    private fun agendar(token: String, corpo: String) = mockMvc.perform(
        post("/api/consultas")
            .header("Authorization", bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo),
    )

    @Test
    fun `recepcionista agenda consulta e ela aparece na agenda`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO, nome = "Dra. Camila")
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0)

        agendar(token, corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, data))
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
        val corpo = corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, data)

        agendar(token, corpo).andExpect(status().isCreated)

        agendar(token, corpo)
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.mensagem").value("O veterinário já possui uma consulta neste horário."))
    }

    @Test
    fun `horarios no mesmo minuto com segundos diferentes conflitam`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")
        val base = LocalDateTime.now().plusDays(4).withHour(8).withMinute(0)

        agendar(token, corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, base.withSecond(15)))
            .andExpect(status().isCreated)

        agendar(token, corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, base.withSecond(45)))
            .andExpect(status().isConflict)
    }

    @Test
    fun `veterinario enxerga apenas a propria agenda`() {
        val vetA = criarUsuario("veta@teste.com", Perfil.VETERINARIO, nome = "Vet A")
        val vetB = criarUsuario("vetb@teste.com", Perfil.VETERINARIO, nome = "Vet B")
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val tokenRecepcao = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0)

        agendar(tokenRecepcao, corpoConsulta(animal.id!!, tutor.id!!, vetA.id!!, data))
            .andExpect(status().isCreated)
        agendar(tokenRecepcao, corpoConsulta(animal.id!!, tutor.id!!, vetB.id!!, data.plusHours(1)))
            .andExpect(status().isCreated)

        val tokenVetA = login("veta@teste.com")
        mockMvc.perform(
            get("/api/consultas")
                .param("inicio", data.toLocalDate().toString())
                .param("fim", data.toLocalDate().toString())
                .header("Authorization", bearer(tokenVetA)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].veterinarioNome").value("Vet A"))
    }

    @Test
    fun `animal de outro tutor retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutorA = criarTutor("Tutor A", "11144477735")
        val tutorB = criarTutor("Tutor B", "39053344705")
        val animalDeA = criarAnimal(tutorA)
        val token = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(11)

        agendar(token, corpoConsulta(animalDeA.id!!, tutorB.id!!, vet.id!!, data))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.mensagem").value("O animal selecionado não pertence ao tutor informado."))
    }

    @Test
    fun `veterinario nao pode agendar consulta`() {
        criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val token = login("vet@teste.com")

        agendar(token, corpoConsulta(1, 1, 1, LocalDateTime.now().plusDays(1)))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `consulta no passado retorna 400`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")

        agendar(token, corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, LocalDateTime.of(2020, 1, 1, 10, 0)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `agenda fica em cache e e invalidada ao agendar`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(13).withMinute(0).withSecond(0).withNano(0)
        val dia = data.toLocalDate().toString()

        mockMvc.perform(
            get("/api/consultas").param("inicio", dia).param("fim", dia)
                .header("Authorization", bearer(token)),
        ).andExpect(status().isOk)

        val cache = cacheManager.getCache("agenda")!!
        org.junit.jupiter.api.Assertions.assertNotNull(cache)
        org.junit.jupiter.api.Assertions.assertTrue(
            (cache as org.springframework.cache.concurrent.ConcurrentMapCache).nativeCache.isNotEmpty(),
        )

        agendar(token, corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, data))
            .andExpect(status().isCreated)

        org.junit.jupiter.api.Assertions.assertTrue(
            (cache as org.springframework.cache.concurrent.ConcurrentMapCache).nativeCache.isEmpty(),
        )
    }

    @Test
    fun `recepcionista cancela consulta futura e horario fica livre`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val token = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0)
        val corpo = corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, data)

        val criada = agendar(token, corpo)
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        val id = com.jayway.jsonpath.JsonPath.read<Int>(criada, "$.id")

        mockMvc.perform(
            post("/api/consultas/$id/cancelar")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"motivo":"Tutor pediu para remarcar"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELADA"))
            .andExpect(jsonPath("$.motivoCancelamento").value("Tutor pediu para remarcar"))

        // o mesmo horário pode ser reutilizado após o cancelamento
        agendar(token, corpo).andExpect(status().isCreated)
    }

    @Test
    fun `veterinario cancela a propria consulta mas nao a de outro`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val outro = criarUsuario("outro@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val tokenRecepcao = login("recepcao@teste.com")
        val data = LocalDateTime.now().plusDays(2).withHour(16).withMinute(0).withSecond(0).withNano(0)
        val criada = agendar(tokenRecepcao, corpoConsulta(animal.id!!, tutor.id!!, vet.id!!, data))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        val id = com.jayway.jsonpath.JsonPath.read<Int>(criada, "$.id")

        mockMvc.perform(
            post("/api/consultas/$id/cancelar")
                .header("Authorization", bearer(login("outro@teste.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"motivo":"Indisponibilidade"}"""),
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            post("/api/consultas/$id/cancelar")
                .header("Authorization", bearer(login("vet@teste.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"motivo":"Indisponibilidade"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELADA"))
            .andExpect(jsonPath("$.motivoCancelamento").value("Indisponibilidade"))
    }

    @Test
    fun `consulta inexistente nao pode ser cancelada`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/consultas/9999/cancelar")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"),
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `consulta passada nao pode ser cancelada`() {
        criarUsuario("recepcao@teste.com", Perfil.RECEPCIONISTA)
        val vet = criarUsuario("vet@teste.com", Perfil.VETERINARIO)
        val tutor = criarTutor()
        val animal = criarAnimal(tutor)
        val consulta = consultas.save(
            br.ufba.vetcare.domain.Consulta(
                animal = animal,
                tutor = tutor,
                veterinario = vet,
                dataHora = LocalDateTime.now().minusDays(3),
                horarioAtivo = LocalDateTime.now().minusDays(3),
            ),
        )
        val token = login("recepcao@teste.com")

        mockMvc.perform(
            post("/api/consultas/${consulta.id}/cancelar")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"),
        ).andExpect(status().isBadRequest)
    }
}
