package br.ufba.vetcare.infrastructure.security

import br.ufba.vetcare.domain.Usuario
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UsuarioDetailsService(private val repository: UsuarioRepository) {

    fun carregar(email: String): UsuarioAutenticado? =
        repository.findByEmailIgnoreCase(email).orElse(null)?.toAutenticado()

    fun carregarPorEmail(email: String): UsuarioAutenticado =
        carregar(email) ?: throw UsernameNotFoundException("Usuário não encontrado")

    fun Usuario.toAutenticado() = UsuarioAutenticado(
        id = id ?: 0,
        nome = nome,
        email = email,
        perfil = perfil,
        ativo = ativo,
    )
}
