package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.LoginRequest
import br.ufba.vetcare.api.dto.LoginResponse
import br.ufba.vetcare.api.dto.UsuarioResposta
import br.ufba.vetcare.application.exception.AutenticacaoException
import br.ufba.vetcare.domain.repository.UsuarioRepository
import br.ufba.vetcare.infrastructure.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val auditoriaService: AuditoriaService,
) {

    fun login(request: LoginRequest): LoginResponse {
        val usuario = usuarioRepository.findByEmailIgnoreCase(request.email.trim()).orElse(null)
        if (usuario == null || !passwordEncoder.matches(request.senha, usuario.senha)) {
            auditoriaService.registrar(
                acao = "LOGIN_FALHA",
                entidade = "Usuario",
                detalhes = "Tentativa de login com e-mail ${request.email}",
                usuarioEmail = request.email,
            )
            throw AutenticacaoException("E-mail ou senha inválidos.")
        }
        if (!usuario.ativo) {
            throw AutenticacaoException("Usuário inativo. Procure um administrador.")
        }
        auditoriaService.registrar(
            acao = "LOGIN_SUCESSO",
            entidade = "Usuario",
            entidadeId = usuario.id,
            usuarioEmail = usuario.email,
        )
        return LoginResponse(
            token = jwtService.gerarToken(usuario),
            expiraEmSegundos = jwtService.expiracaoEmSegundos(),
            usuario = UsuarioResposta.de(usuario),
        )
    }

    fun usuarioAtual(email: String): UsuarioResposta =
        usuarioRepository.findByEmailIgnoreCase(email)
            .map(UsuarioResposta::de)
            .orElseThrow { AutenticacaoException("Sessão inválida.") }
}
