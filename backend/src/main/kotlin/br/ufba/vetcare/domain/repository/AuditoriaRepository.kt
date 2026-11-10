package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Auditoria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface AuditoriaRepository : JpaRepository<Auditoria, Long> {
    @Query(
        """
        select a from Auditoria a
        where a.criadoEm >= :inicio
          and a.criadoEm <= :fim
          and (:acao = '' or lower(a.acao) like lower(concat('%', :acao, '%')))
          and (:usuario = '' or lower(coalesce(a.usuarioEmail, '')) like lower(concat('%', :usuario, '%')))
        order by a.criadoEm desc
        """,
    )
    fun buscar(
        @Param("inicio") inicio: Instant,
        @Param("fim") fim: Instant,
        @Param("acao") acao: String,
        @Param("usuario") usuario: String,
        pageable: Pageable,
    ): Page<Auditoria>
}
