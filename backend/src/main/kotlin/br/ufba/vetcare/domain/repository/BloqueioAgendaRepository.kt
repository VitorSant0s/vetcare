package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.BloqueioAgenda
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional

interface BloqueioAgendaRepository : JpaRepository<BloqueioAgenda, Long> {
    fun existsByVeterinarioIdAndData(veterinarioId: Long, data: LocalDate): Boolean
    fun findByVeterinarioIdAndData(veterinarioId: Long, data: LocalDate): Optional<BloqueioAgenda>

    @EntityGraph(attributePaths = ["veterinario"])
    fun findByVeterinarioIdAndDataBetweenOrderByDataAsc(
        veterinarioId: Long,
        inicio: LocalDate,
        fim: LocalDate,
    ): List<BloqueioAgenda>
}
