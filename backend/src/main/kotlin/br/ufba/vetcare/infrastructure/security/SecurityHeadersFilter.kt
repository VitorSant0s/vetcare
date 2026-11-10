package br.ufba.vetcare.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Cabeçalhos de endurecimento HTTP aplicados a todas as respostas.
 */
@Component
class SecurityHeadersFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        response.setHeader("X-Content-Type-Options", "nosniff")
        response.setHeader("X-Frame-Options", "DENY")
        response.setHeader("Referrer-Policy", "no-referrer")
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()")
        response.setHeader("Cross-Origin-Resource-Policy", "same-site")
        if (request.requestURI.startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-store")
        }
        filterChain.doFilter(request, response)
    }
}
