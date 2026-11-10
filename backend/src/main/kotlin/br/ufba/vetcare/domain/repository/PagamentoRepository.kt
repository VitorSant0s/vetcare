package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Pagamento
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface PagamentoRepository : JpaRepository<Pagamento, Long> {
    @EntityGraph(attributePaths = ["tutor", "consulta"])
    fun findByDataPagamentoBetweenOrderByDataPagamentoDesc(inicio: LocalDate, fim: LocalDate): List<Pagamento>

    @EntityGraph(attributePaths = ["tutor", "consulta"])
    fun findAllByOrderByCriadoEmDesc(): List<Pagamento>

    @EntityGraph(attributePaths = ["tutor", "consulta"])
    fun findByTutorIdOrderByDataPagamentoDesc(tutorId: Long): List<Pagamento>
}
