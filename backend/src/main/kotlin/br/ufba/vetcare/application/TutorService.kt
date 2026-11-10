package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.api.dto.TutorRequest
import br.ufba.vetcare.api.dto.TutorResposta
import br.ufba.vetcare.api.dto.toPageResponse
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Cpf
import br.ufba.vetcare.domain.Tutor
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TutorService(
    private val repository: TutorRepository,
    private val animalRepository: AnimalRepository,
    private val consultaRepository: ConsultaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listar(termo: String?, page: Int, size: Int): PageResponse<TutorResposta> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by("nome"))
        val resultado = if (termo.isNullOrBlank()) {
            repository.findAll(pageable)
        } else {
            repository.findByNomeContainingIgnoreCaseOrCpfContainingOrEmailContainingIgnoreCase(
                termo.trim(),
                termo.trim(),
                termo.trim(),
                pageable,
            )
        }
        return resultado.map(TutorResposta::de).toPageResponse()
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): TutorResposta = TutorResposta.de(obter(id))

    @Transactional
    fun criar(request: TutorRequest): TutorResposta {
        val cpf = validarCpf(request.cpf)
        if (repository.existsByCpf(cpf)) {
            throw ConflitoException("Já existe um tutor com este CPF.")
        }
        val tutor = repository.save(
            Tutor(
                nome = request.nome.trim(),
                cpf = cpf,
                email = request.email.trim().lowercase(),
                telefone = request.telefone.trim(),
                endereco = request.endereco?.trim()?.takeIf { it.isNotEmpty() },
                senha = request.senha?.takeIf { it.isNotBlank() }?.let { passwordEncoder.encode(it)!! },
                ativo = request.ativo,
            ),
        )
        auditoriaService.registrar("TUTOR_CRIADO", "Tutor", tutor.id, tutor.nome)
        return TutorResposta.de(tutor)
    }

    @Transactional
    fun atualizar(id: Long, request: TutorRequest): TutorResposta {
        val tutor = obter(id)
        val cpf = validarCpf(request.cpf)
        repository.findByCpf(cpf).ifPresent {
            if (it.id != id) throw ConflitoException("Já existe um tutor com este CPF.")
        }
        tutor.nome = request.nome.trim()
        tutor.cpf = cpf
        tutor.email = request.email.trim().lowercase()
        tutor.telefone = request.telefone.trim()
        tutor.endereco = request.endereco?.trim()?.takeIf { it.isNotEmpty() }
        tutor.ativo = request.ativo
        request.senha?.takeIf { it.isNotBlank() }?.let {
            tutor.senha = passwordEncoder.encode(it)!!
            tutor.primeiroAcesso = true
        }
        tutor.atualizadoEm = Instant.now()
        val salvo = repository.save(tutor)
        auditoriaService.registrar("TUTOR_ATUALIZADO", "Tutor", salvo.id)
        return TutorResposta.de(salvo)
    }

    @Transactional
    fun excluir(id: Long) {
        val tutor = obter(id)
        val animais = animalRepository.countByTutorId(id)
        if (animais > 0) {
            throw ConflitoException("Não é possível excluir um tutor que possui animais cadastrados.")
        }
        if (consultaRepository.existsByTutorId(id)) {
            throw ConflitoException("Não é possível excluir um tutor que possui consultas registradas.")
        }
        repository.delete(tutor)
        auditoriaService.registrar("TUTOR_EXCLUIDO", "Tutor", id, tutor.nome)
    }

    private fun obter(id: Long): Tutor =
        repository.findById(id).orElseThrow { RecursoNaoEncontradoException("Tutor não encontrado.") }

    private fun validarCpf(cpf: String): String {
        if (!Cpf.valido(cpf)) {
            throw RegraNegocioException("CPF inválido.")
        }
        return Cpf.normalizar(cpf)
    }
}
