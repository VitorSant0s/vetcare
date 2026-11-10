package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.LoginRequest
import br.ufba.vetcare.api.dto.LoginResponse
import br.ufba.vetcare.api.dto.UsuarioResposta
import br.ufba.vetcare.application.exception.AutenticacaoException
import br.ufba.vetcare.domain.repository.UsuarioRepository
import br.ufba.vetcare.infrastructure.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val auditoriaService: AuditoriaService,
    private val rateLimiter: LoginRateLimiter,
) {

    fun login(request: LoginRequest): LoginResponse {
        rateLimiter.verificar(request.email)
        val usuario = usuarioRepository.findByEmailIgnoreCase(request.email.trim()).orElse(null)
        if (usuario == null || !passwordEncoder.matches(request.senha, usuario.senha)) {
            val bloqueou = rateLimiter.registrarFalha(request.email)
            auditoriaService.registrar(
                acao = "LOGIN_FALHA",
                entidade = "Usuario",
                detalhes = "Tentativa de login com e-mail ${request.email}",
                usuarioEmail = request.email,
            )
            if (bloqueou) {
                auditoriaService.registrar(
                    acao = "LOGIN_BLOQUEADO",
                    entidade = "Usuario",
                    detalhes = "Excesso de tentativas para ${request.email}",
                    usuarioEmail = request.email,
                )
            }
            throw AutenticacaoException("E-mail ou senha inválidos.")
        }
        if (!usuario.ativo) {
            throw AutenticacaoException("Usuário inativo. Procure um administrador.")
        }
        rateLimiter.limpar(request.email)
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

    @Transactional
    fun logout(email: String) {
        val usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null) ?: return
        usuario.tokenVersion += 1
        usuarioRepository.save(usuario)
        auditoriaService.registrar(
            acao = "LOGOUT",
            entidade = "Usuario",
            entidadeId = usuario.id,
            usuarioEmail = usuario.email,
        )
    }

    fun usuarioAtual(email: String): UsuarioResposta =
        usuarioRepository.findByEmailIgnoreCase(email)
            .map(UsuarioResposta::de)
            .orElseThrow { AutenticacaoException("Sessão inválida.") }
}
