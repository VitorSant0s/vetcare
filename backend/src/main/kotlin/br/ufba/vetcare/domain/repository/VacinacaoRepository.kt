package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Vacinacao
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional

interface VacinacaoRepository : JpaRepository<Vacinacao, Long> {
    @EntityGraph(attributePaths = ["animal", "veterinario"])
    fun findByAnimalIdOrderByDataAplicacaoDesc(animalId: Long): List<Vacinacao>

    @EntityGraph(attributePaths = ["animal", "veterinario"])
    override fun findById(id: Long): Optional<Vacinacao>

    fun countByAnimalId(animalId: Long): Long

    @EntityGraph(attributePaths = ["animal", "veterinario"])
    fun findByProximaDoseBetween(inicio: LocalDate, fim: LocalDate): List<Vacinacao>
}
