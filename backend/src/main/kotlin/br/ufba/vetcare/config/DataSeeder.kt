package br.ufba.vetcare.config

import br.ufba.vetcare.domain.Animal
import br.ufba.vetcare.domain.Consulta
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Tutor
import br.ufba.vetcare.domain.Usuario
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Dados de demonstração para a apresentação. Não roda quando app.seed.enabled=false.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = ["enabled"], havingValue = "true")
class DataSeeder(
    private val usuarios: UsuarioRepository,
    private val tutores: TutorRepository,
    private val animais: AnimalRepository,
    private val consultas: ConsultaRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        if (usuarios.count() > 0) return

        usuarios.save(
            Usuario(
                nome = "Administrador VetCare",
                email = "admin@vetcare.com",
                senha = passwordEncoder.encode("Admin@123")!!,
                perfil = Perfil.ADMINISTRADOR,
            ),
        )
        usuarios.save(
            Usuario(
                nome = "Marina Recepção",
                email = "recepcao@vetcare.com",
                senha = passwordEncoder.encode("Recepcao@123")!!,
                perfil = Perfil.RECEPCIONISTA,
            ),
        )
        val veterinario = usuarios.save(
            Usuario(
                nome = "Dra. Camila Andrade",
                email = "vet@vetcare.com",
                senha = passwordEncoder.encode("Vet@123")!!,
                perfil = Perfil.VETERINARIO,
            ),
        )

        val ana = tutores.save(
            Tutor(
                nome = "Ana Paula Souza",
                cpf = "12345678909",
                email = "ana.souza@email.com",
                telefone = "(71) 98888-1111",
                endereco = "Rua das Acácias, 120 - Salvador/BA",
            ),
        )
        val bruno = tutores.save(
            Tutor(
                nome = "Bruno Carvalho",
                cpf = "98765432100",
                email = "bruno.carvalho@email.com",
                telefone = "(71) 97777-2222",
                endereco = "Av. Sete de Setembro, 45 - Salvador/BA",
            ),
        )

        val thor = animais.save(
            Animal(
                nome = "Thor",
                especie = "Cão",
                raca = "Golden Retriever",
                sexo = "Macho",
                dataNascimento = LocalDate.of(2022, 3, 14),
                tutor = ana,
            ),
        )
        animais.save(
            Animal(
                nome = "Luna",
                especie = "Gato",
                raca = "Siamês",
                sexo = "Fêmea",
                dataNascimento = LocalDate.of(2021, 8, 2),
                tutor = ana,
            ),
        )
        animais.save(
            Animal(
                nome = "Bolt",
                especie = "Cão",
                raca = "Beagle",
                sexo = "Macho",
                dataNascimento = LocalDate.of(2020, 11, 20),
                tutor = bruno,
            ),
        )

        consultas.save(
            Consulta(
                animal = thor,
                tutor = ana,
                veterinario = veterinario,
                dataHora = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0),
                observacoes = "Retorno para avaliação vacinal.",
            ),
        )

        log.info(
            "Seed de demonstração criado com {} usuários, {} tutores e {} animais. " +
                "As credenciais estão documentadas no README; desative APP_SEED em produção.",
            usuarios.count(),
            tutores.count(),
            animais.count(),
        )
    }
}
