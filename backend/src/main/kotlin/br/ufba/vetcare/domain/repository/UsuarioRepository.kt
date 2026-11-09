package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmailIgnoreCase(email: String): Optional<Usuario>
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun findAllByPerfilAndAtivoTrue(perfil: Perfil): List<Usuario>
    fun countByPerfilAndAtivoTrue(perfil: Perfil): Long
    fun findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(nome: String, email: String, pageable: Pageable): Page<Usuario>
    fun findByPerfilAndAtivoTrueAndNomeContainingIgnoreCase(perfil: Perfil, nome: String): List<Usuario>
}
