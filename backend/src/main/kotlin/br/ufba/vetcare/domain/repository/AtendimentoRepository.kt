package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Atendimento
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AtendimentoRepository : JpaRepository<Atendimento, Long> {
    fun existsByConsultaId(consultaId: Long): Boolean
    fun countByDataHoraBetween(inicio: java.time.LocalDateTime, fim: java.time.LocalDateTime): Long
    fun findByConsultaId(consultaId: Long): Optional<Atendimento>

    @EntityGraph(attributePaths = ["animal", "veterinario", "prescricoes"])
    fun findByAnimalIdOrderByDataHoraDesc(animalId: Long): List<Atendimento>

    @EntityGraph(attributePaths = ["animal", "veterinario", "prescricoes"])
    override fun findById(id: Long): Optional<Atendimento>
}
