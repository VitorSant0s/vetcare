package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Tutor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface TutorRepository : JpaRepository<Tutor, Long> {
    fun findByCpf(cpf: String): Optional<Tutor>
    fun existsByCpf(cpf: String): Boolean
    fun findByNomeContainingIgnoreCaseOrCpfContainingOrEmailContainingIgnoreCase(
        nome: String,
        cpf: String,
        email: String,
        pageable: Pageable,
    ): Page<Tutor>
}
