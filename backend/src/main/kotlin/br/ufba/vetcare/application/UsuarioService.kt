package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.api.dto.UsuarioRequest
import br.ufba.vetcare.api.dto.UsuarioResposta
import br.ufba.vetcare.api.dto.toPageResponse
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Usuario
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UsuarioService(
    private val repository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listar(termo: String?, page: Int, size: Int): PageResponse<UsuarioResposta> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by("nome"))
        val resultado = if (termo.isNullOrBlank()) {
            repository.findAll(pageable)
        } else {
            repository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(termo.trim(), termo.trim(), pageable)
        }
        return resultado.map(UsuarioResposta::de).toPageResponse()
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): UsuarioResposta =
        UsuarioResposta.de(obter(id))

    fun veterinarios(): List<UsuarioResposta> =
        repository.findAllByPerfilAndAtivoTrue(br.ufba.vetcare.domain.Perfil.VETERINARIO)
            .sortedBy { it.nome }
            .map(UsuarioResposta::de)

    @Transactional
    fun criar(request: UsuarioRequest): UsuarioResposta {
        val email = request.email.trim().lowercase()
        if (repository.existsByEmailIgnoreCase(email)) {
            throw ConflitoException("Já existe um usuário com este e-mail.")
        }
        val senha = request.senha ?: throw RegraNegocioException("Informe a senha do novo usuário.")
        val usuario = Usuario(
            nome = request.nome.trim(),
            email = email,
            senha = passwordEncoder.encode(senha)!!,
            perfil = request.perfil ?: throw RegraNegocioException("Informe o perfil do usuário."),
            ativo = request.ativo,
        )
        val salvo = repository.save(usuario)
        auditoriaService.registrar("USUARIO_CRIADO", "Usuario", salvo.id, "Perfil ${salvo.perfil}")
        return UsuarioResposta.de(salvo)
    }

    @Transactional
    fun atualizar(id: Long, request: UsuarioRequest): UsuarioResposta {
        val usuario = obter(id)
        val email = request.email.trim().lowercase()
        repository.findByEmailIgnoreCase(email).ifPresent {
            if (it.id != id) throw ConflitoException("Já existe um usuário com este e-mail.")
        }
        usuario.nome = request.nome.trim()
        usuario.email = email
        usuario.perfil = request.perfil ?: usuario.perfil
        usuario.ativo = request.ativo
        request.senha?.takeIf { it.isNotBlank() }?.let { usuario.senha = passwordEncoder.encode(it)!! }
        usuario.atualizadoEm = Instant.now()
        val salvo = repository.save(usuario)
        auditoriaService.registrar("USUARIO_ATUALIZADO", "Usuario", salvo.id)
        return UsuarioResposta.de(salvo)
    }

    /**
     * Exclusão lógica: preserva o histórico de consultas e a trilha de auditoria.
     */
    @Transactional
    fun excluir(id: Long) {
        val usuario = obter(id)
        usuario.ativo = false
        usuario.atualizadoEm = Instant.now()
        repository.save(usuario)
        auditoriaService.registrar("USUARIO_EXCLUIDO", "Usuario", id)
    }

    private fun obter(id: Long): Usuario =
        repository.findById(id).orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
}
