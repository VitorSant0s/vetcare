package br.ufba.vetcare.application

import br.ufba.vetcare.domain.Auditoria
import br.ufba.vetcare.domain.repository.AuditoriaRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
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
    ) = salvar(acao, entidade, entidadeId, detalhes, usuarioEmail)

    /**
     * Registra eventos de segurança que devem sobreviver ao rollback da operação
     * (acesso negado, código inválido, tentativa bloqueada).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun registrarIndependente(
        acao: String,
        entidade: String? = null,
        entidadeId: Long? = null,
        detalhes: String? = null,
        usuarioEmail: String? = emailAtual(),
    ) = salvar(acao, entidade, entidadeId, detalhes, usuarioEmail)

    private fun salvar(
        acao: String,
        entidade: String?,
        entidadeId: Long?,
        detalhes: String?,
        usuarioEmail: String?,
    ) {
        repository.save(
            Auditoria(
                usuarioEmail = usuarioEmail?.take(160),
                acao = acao,
                entidade = entidade,
                entidadeId = entidadeId,
                detalhes = detalhes?.take(500),
                ip = ipAtual()?.take(45),
            ),
        )
    }

    private fun emailAtual(): String? {
        val autenticacao = SecurityContextHolder.getContext().authentication ?: return null
        if (!autenticacao.isAuthenticated) return null
        return when (val principal = autenticacao.principal) {
            is br.ufba.vetcare.infrastructure.security.UsuarioAutenticado -> principal.email
            is br.ufba.vetcare.infrastructure.security.TutorAutenticado ->
                principal.email.ifBlank { principal.cpf }
            else -> autenticacao.name
        }
    }

    private fun ipAtual(): String? {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request ?: return null
        return request.getHeader("X-Forwarded-For")?.substringBefore(',')?.trim()
            ?: (request as HttpServletRequest).remoteAddr
    }
}
