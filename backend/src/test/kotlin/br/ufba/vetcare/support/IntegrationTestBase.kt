package br.ufba.vetcare.support

import br.ufba.vetcare.domain.Animal
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Tutor
import br.ufba.vetcare.domain.Usuario
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class IntegrationTestBase {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var usuarios: UsuarioRepository

    @Autowired
    protected lateinit var tutores: TutorRepository

    @Autowired
    protected lateinit var animais: AnimalRepository

    @Autowired
    protected lateinit var consultas: ConsultaRepository

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    protected fun criarUsuario(
        email: String,
        perfil: Perfil,
        senha: String = "Senha@123",
        nome: String = "Usuário Teste",
        ativo: Boolean = true,
    ): Usuario = usuarios.save(
        Usuario(
            nome = nome,
            email = email,
            senha = passwordEncoder.encode(senha)!!,
            perfil = perfil,
            ativo = ativo,
        ),
    )

    protected fun criarTutor(nome: String = "Tutor Teste", cpf: String = "11122233344"): Tutor = tutores.save(
        Tutor(
            nome = nome,
            cpf = cpf,
            email = "tutor@teste.com",
            telefone = "(71) 99999-0000",
            endereco = "Rua Teste, 1",
        ),
    )

    protected fun criarAnimal(tutor: Tutor, nome: String = "Rex"): Animal = animais.save(
        Animal(
            nome = nome,
            especie = "Cão",
            raca = "Vira-lata",
            sexo = "Macho",
            dataNascimento = LocalDate.of(2021, 1, 1),
            tutor = tutor,
        ),
    )

    protected fun login(email: String, senha: String = "Senha@123"): String {
        val resposta = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","senha":"$senha"}"""),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        return JsonPath.read(resposta, "$.token")
    }

    protected fun bearer(token: String): String = "Bearer $token"
}
