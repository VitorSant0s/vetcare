package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Consulta
import br.ufba.vetcare.domain.StatusConsulta
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ConsultaRepository : JpaRepository<Consulta, Long> {
    @EntityGraph(attributePaths = ["animal", "tutor", "veterinario"])
    @Query(
        """
        select c from Consulta c
        where c.dataHora >= :inicio and c.dataHora < :fim
          and (:veterinarioId is null or c.veterinario.id = :veterinarioId)
        order by c.dataHora
        """,
    )
    fun listarAgenda(
        @Param("inicio") inicio: LocalDateTime,
        @Param("fim") fim: LocalDateTime,
        @Param("veterinarioId") veterinarioId: Long?,
    ): List<Consulta>

    fun existsByVeterinarioIdAndDataHoraAndStatusNot(
        veterinarioId: Long,
        dataHora: LocalDateTime,
        status: StatusConsulta,
    ): Boolean

    fun existsByAnimalIdAndDataHoraAfterAndStatus(
        animalId: Long,
        dataHora: LocalDateTime,
        status: StatusConsulta,
    ): Boolean

    fun existsByTutorId(tutorId: Long): Boolean

    @EntityGraph(attributePaths = ["animal", "tutor", "veterinario"])
    fun findByAnimalIdOrderByDataHoraDesc(animalId: Long): List<Consulta>

    @EntityGraph(attributePaths = ["animal", "tutor", "veterinario"])
    fun findByDataHoraBetween(inicio: LocalDateTime, fim: LocalDateTime): List<Consulta>

    fun existsByVeterinarioIdAndDataHoraAfterAndStatus(
        veterinarioId: Long,
        dataHora: LocalDateTime,
        status: StatusConsulta,
    ): Boolean

    @EntityGraph(attributePaths = ["animal", "tutor", "veterinario"])
    fun findByIdAndVeterinarioId(id: Long, veterinarioId: Long): Consulta?

    @EntityGraph(attributePaths = ["animal", "tutor", "veterinario"])
    override fun findAll(): List<Consulta>
}
