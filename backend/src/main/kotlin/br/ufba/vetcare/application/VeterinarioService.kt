package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.VeterinarioRequest
import br.ufba.vetcare.api.dto.VeterinarioResposta
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.StatusConsulta
import br.ufba.vetcare.domain.Usuario
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime

/**
 * REQ06 - Manter Veterinário. O profissional é um usuário com perfil VETERINARIO
 * acrescido de CRMV e especialidade (ADR-10/ADR-13).
 */
@Service
class VeterinarioService(
    private val repository: UsuarioRepository,
    private val consultaRepository: ConsultaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listar(incluirInativos: Boolean): List<VeterinarioResposta> {
        val lista = if (incluirInativos) {
            repository.findAllByPerfilOrderByNome(Perfil.VETERINARIO)
        } else {
            repository.findAllByPerfilAndAtivoTrue(Perfil.VETERINARIO)
        }
        return lista.map(VeterinarioResposta::de)
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): VeterinarioResposta = VeterinarioResposta.de(obter(id))

    @Transactional
    fun criar(request: VeterinarioRequest): VeterinarioResposta {
        val email = request.email.trim().lowercase()
        if (repository.existsByEmailIgnoreCase(email)) {
            throw ConflitoException("Já existe um usuário com este e-mail.")
        }
        val crmv = request.crmv.trim()
        if (repository.existsByCrmvIgnoreCase(crmv)) {
            throw ConflitoException("Já existe um veterinário com este CRMV.")
        }
        val senha = request.senha ?: throw RegraNegocioException("Informe a senha inicial do veterinário.")
        val veterinario = repository.save(
            Usuario(
                nome = request.nome.trim(),
                email = email,
                senha = passwordEncoder.encode(senha)!!,
                perfil = Perfil.VETERINARIO,
                crmv = crmv,
                especialidade = request.especialidade?.trim()?.takeIf { it.isNotEmpty() },
                ativo = request.ativo,
            ),
        )
        auditoriaService.registrar("VETERINARIO_CRIADO", "Usuario", veterinario.id, crmv)
        return VeterinarioResposta.de(veterinario)
    }

    @Transactional
    fun atualizar(id: Long, request: VeterinarioRequest): VeterinarioResposta {
        val veterinario = obter(id)
        val email = request.email.trim().lowercase()
        repository.findByEmailIgnoreCase(email).ifPresent {
            if (it.id != id) throw ConflitoException("Já existe um usuário com este e-mail.")
        }
        val crmv = request.crmv.trim()
        repository.findAllByPerfilOrderByNome(Perfil.VETERINARIO).firstOrNull {
            it.id != id && it.crmv?.equals(crmv, ignoreCase = true) == true
        }?.let { throw ConflitoException("Já existe um veterinário com este CRMV.") }

        if (!request.ativo && possuiConsultasFuturas(id)) {
            throw ConflitoException("Não é possível desativar um veterinário com consultas futuras agendadas.")
        }

        veterinario.nome = request.nome.trim()
        veterinario.email = email
        veterinario.crmv = crmv
        veterinario.especialidade = request.especialidade?.trim()?.takeIf { it.isNotEmpty() }
        veterinario.ativo = request.ativo
        request.senha?.takeIf { it.isNotBlank() }?.let { veterinario.senha = passwordEncoder.encode(it)!! }
        veterinario.atualizadoEm = Instant.now()
        val salvo = repository.save(veterinario)
        auditoriaService.registrar("VETERINARIO_ATUALIZADO", "Usuario", salvo.id)
        return VeterinarioResposta.de(salvo)
    }

    @Transactional
    fun excluir(id: Long) {
        val veterinario = obter(id)
        if (possuiConsultasFuturas(id)) {
            throw ConflitoException("Não é possível desativar um veterinário com consultas futuras agendadas.")
        }
        veterinario.ativo = false
        veterinario.atualizadoEm = Instant.now()
        repository.save(veterinario)
        auditoriaService.registrar("VETERINARIO_EXCLUIDO", "Usuario", id)
    }

    private fun possuiConsultasFuturas(id: Long): Boolean =
        consultaRepository.existsByVeterinarioIdAndDataHoraAfterAndStatus(
            id,
            LocalDateTime.now(),
            StatusConsulta.AGENDADA,
        )

    private fun obter(id: Long): Usuario {
        val usuario = repository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Veterinário não encontrado.") }
        if (usuario.perfil != Perfil.VETERINARIO) {
            throw RecursoNaoEncontradoException("Veterinário não encontrado.")
        }
        return usuario
    }
}
