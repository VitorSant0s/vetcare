package br.ufba.vetcare.config

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Usuario
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Cria o administrador inicial a partir das variáveis ADMIN_EMAIL / ADMIN_PASSWORD.
 * Não cria dados de demonstração e só age quando ainda não existe administrador ativo.
 */
@Component
@ConditionalOnProperty(prefix = "app.admin", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AdminBootstrap(
    private val repository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.admin.email:}") private val email: String,
    @Value("\${app.admin.password:}") private val senha: String,
    @Value("\${app.admin.nome:Administrador}") private val nome: String,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        if (repository.countByPerfilAndAtivoTrue(Perfil.ADMINISTRADOR) > 0) return
        if (email.isBlank() || senha.isBlank()) {
            log.warn(
                "Nenhum administrador ativo e ADMIN_EMAIL/ADMIN_PASSWORD não configurados. " +
                    "Defina as variáveis no .env para criar o acesso inicial.",
            )
            return
        }
        if (senha.length < 8) {
            log.warn("ADMIN_PASSWORD deve ter ao menos 8 caracteres; administrador inicial não foi criado.")
            return
        }
        repository.save(
            Usuario(
                nome = nome,
                email = email.trim().lowercase(),
                senha = passwordEncoder.encode(senha)!!,
                perfil = Perfil.ADMINISTRADOR,
            ),
        )
        log.info("Administrador inicial criado para {}", email)
    }
}
