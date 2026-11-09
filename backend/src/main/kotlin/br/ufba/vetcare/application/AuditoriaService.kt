package br.ufba.vetcare.application

import br.ufba.vetcare.domain.Auditoria
import br.ufba.vetcare.domain.repository.AuditoriaRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Componente de Auditoria/Log (ADR-05): centraliza o registro de eventos relevantes.
 */
@Service
class AuditoriaService(private val repository: AuditoriaRepository) {

    fun registrar(
        acao: String,
        entidade: String? = null,
        entidadeId: Long? = null,
        detalhes: String? = null,
        usuarioEmail: String? = emailAtual(),
    ) {
        repository.save(
            Auditoria(
                usuarioEmail = usuarioEmail,
                acao = acao,
                entidade = entidade,
                entidadeId = entidadeId,
                detalhes = detalhes?.take(500),
                ip = ipAtual(),
            ),
        )
    }

    private fun emailAtual(): String? =
        SecurityContextHolder.getContext().authentication?.takeIf { it.isAuthenticated }?.name

    private fun ipAtual(): String? {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request ?: return null
        return request.getHeader("X-Forwarded-For")?.substringBefore(',')?.trim()
            ?: (request as HttpServletRequest).remoteAddr
    }
}
