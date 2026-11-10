package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.CompartilhamentoProntuario
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CompartilhamentoProntuarioRepository : JpaRepository<CompartilhamentoProntuario, Long> {
    @EntityGraph(attributePaths = ["animal", "criadoPor"])
    fun findByCodigoIgnoreCase(codigo: String): Optional<CompartilhamentoProntuario>

    @EntityGraph(attributePaths = ["animal", "criadoPor"])
    fun findByAnimalIdAndAtivoTrueOrderByCriadoEmDesc(animalId: Long): List<CompartilhamentoProntuario>

    fun existsByCodigo(codigo: String): Boolean
}
