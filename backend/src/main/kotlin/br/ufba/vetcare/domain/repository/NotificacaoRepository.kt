package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Notificacao
import br.ufba.vetcare.domain.StatusNotificacao
import br.ufba.vetcare.domain.TipoNotificacao
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface NotificacaoRepository : JpaRepository<Notificacao, Long> {
    @EntityGraph(attributePaths = ["tutor", "consulta", "vacinacao"])
    fun findAllByOrderByCriadoEmDesc(): List<Notificacao>

    @EntityGraph(attributePaths = ["tutor", "consulta", "vacinacao"])
    fun findByStatusOrderByCriadoEm(status: StatusNotificacao): List<Notificacao>

    fun existsByTipoAndConsultaId(tipo: TipoNotificacao, consultaId: Long): Boolean
    fun existsByTipoAndVacinacaoId(tipo: TipoNotificacao, vacinacaoId: Long): Boolean
}
