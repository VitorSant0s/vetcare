package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Animal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AnimalRepository : JpaRepository<Animal, Long> {
    @EntityGraph(attributePaths = ["tutor"])
    fun findAllByAtivoTrue(pageable: Pageable): Page<Animal>

    @EntityGraph(attributePaths = ["tutor"])
    @Query(
        """
        select a from Animal a
        where a.ativo = true
          and (lower(a.nome) like lower(concat('%', :termo, '%'))
               or lower(a.tutor.nome) like lower(concat('%', :termo, '%')))
        """,
    )
    fun buscar(@Param("termo") termo: String, pageable: Pageable): Page<Animal>

    @EntityGraph(attributePaths = ["tutor"])
    fun findAllByTutorIdAndAtivoTrue(tutorId: Long): List<Animal>

    fun countByTutorId(tutorId: Long): Long
}
