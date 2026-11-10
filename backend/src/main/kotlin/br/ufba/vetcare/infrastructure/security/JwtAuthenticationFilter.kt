package br.ufba.vetcare.infrastructure.security

import br.ufba.vetcare.domain.Usuario
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val usuarioDetailsService: UsuarioDetailsService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            try {
                val claims = jwtService.extrairClaims(token)
                if (SecurityContextHolder.getContext().authentication == null) {
                    if (claims["tipo"] == "CLIENTE") {
                        val tutorId = (claims["tutorId"] as? Number)?.toLong()
                        if (tutorId != null) {
                            val principal = TutorAutenticado(
                                id = tutorId,
                                nome = claims["nome"] as? String ?: "",
                                cpf = claims.subject ?: "",
                                email = claims["email"] as? String ?: "",
                            )
                            val authentication = UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                listOf(SimpleGrantedAuthority("ROLE_CLIENTE")),
                            )
                            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = authentication
                        }
                    } else {
                        val email = claims.subject
                        val usuario = email?.let { usuarioDetailsService.carregar(it) }
                        if (usuario != null && usuario.ativo) {
                            val authentication = UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                listOf(SimpleGrantedAuthority("ROLE_${usuario.perfil.name}")),
                            )
                            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = authentication
                        }
                    }
                }
            } catch (_: ExpiredJwtException) {
                // token expirado: a requisição segue sem autenticação e recebe 401
            } catch (_: Exception) {
                // token inválido: idem
            }
        }
        filterChain.doFilter(request, response)
    }
}

/**
 * Usuário autenticado disponível no SecurityContext.
 */
data class UsuarioAutenticado(
    val id: Long,
    val nome: String,
    val email: String,
    val perfil: br.ufba.vetcare.domain.Perfil,
    val ativo: Boolean,
)

data class TutorAutenticado(
    val id: Long,
    val nome: String,
    val cpf: String,
    val email: String,
)
