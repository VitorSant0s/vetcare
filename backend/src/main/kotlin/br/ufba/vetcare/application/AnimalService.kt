package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AnimalRequest
import br.ufba.vetcare.api.dto.AnimalResposta
import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.api.dto.toPageResponse
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Animal
import br.ufba.vetcare.domain.StatusConsulta
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
class AnimalService(
    private val repository: AnimalRepository,
    private val tutorRepository: TutorRepository,
    private val consultaRepository: ConsultaRepository,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listar(termo: String?, page: Int, size: Int, veterinarioId: Long? = null): PageResponse<AnimalResposta> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by("nome"))
        val resultado = when {
            veterinarioId != null && termo.isNullOrBlank() -> repository.findAllByVeterinario(veterinarioId, pageable)
            veterinarioId != null -> repository.buscarPorVeterinario(veterinarioId, termo!!.trim(), pageable)
            termo.isNullOrBlank() -> repository.findAllByAtivoTrue(pageable)
            else -> repository.buscar(termo.trim(), pageable)
        }
        return resultado.map(AnimalResposta::de).toPageResponse()
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long, veterinarioId: Long? = null): AnimalResposta {
        val animal = obter(id)
        if (veterinarioId != null && !consultaRepository.existsByAnimalIdAndVeterinarioId(id, veterinarioId)) {
            throw RecursoNaoEncontradoException("Paciente não encontrado na sua agenda.")
        }
        return AnimalResposta.de(animal)
    }

    @Transactional(readOnly = true)
    fun listarPorTutor(tutorId: Long): List<AnimalResposta> {
        if (!tutorRepository.existsById(tutorId)) {
            throw RecursoNaoEncontradoException("Tutor não encontrado.")
        }
        return repository.findAllByTutorIdAndAtivoTrue(tutorId).map(AnimalResposta::de)
    }

    @Transactional
    fun criar(request: AnimalRequest): AnimalResposta {
        val animal = Animal()
        aplicar(animal, request)
        val salvo = repository.save(animal)
        auditoriaService.registrar("ANIMAL_CRIADO", "Animal", salvo.id, salvo.nome)
        return AnimalResposta.de(salvo)
    }

    @Transactional
    fun atualizar(id: Long, request: AnimalRequest): AnimalResposta {
        val animal = obter(id)
        val tutorAnterior = animal.tutor.id
        val novoTutor = request.tutorId
        if (tutorAnterior != novoTutor && consultaRepository.existsByAnimalIdAndDataHoraAfterAndStatus(
                id,
                java.time.LocalDateTime.now(),
                StatusConsulta.AGENDADA,
            )
        ) {
            throw ConflitoException("Não é possível transferir um animal que possui consultas futuras agendadas.")
        }
        aplicar(animal, request)
        animal.atualizadoEm = Instant.now()
        val salvo = repository.save(animal)
        auditoriaService.registrar("ANIMAL_ATUALIZADO", "Animal", salvo.id)
        return AnimalResposta.de(salvo)
    }

    @Transactional
    fun excluir(id: Long) {
        val animal = obter(id)
        if (consultaRepository.existsByAnimalIdAndDataHoraAfterAndStatus(
                id,
                java.time.LocalDateTime.now(),
                StatusConsulta.AGENDADA,
            )
        ) {
            throw ConflitoException("Não é possível excluir um animal que possui consultas futuras agendadas.")
        }
        animal.ativo = false
        animal.atualizadoEm = Instant.now()
        repository.save(animal)
        auditoriaService.registrar("ANIMAL_EXCLUIDO", "Animal", id, animal.nome)
    }

    private fun aplicar(animal: Animal, request: AnimalRequest) {
        val tutorId = request.tutorId ?: throw RegraNegocioException("Selecione o tutor do animal.")
        val tutor = tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Tutor não encontrado.") }
        request.dataNascimento?.let {
            if (it.isAfter(LocalDate.now())) {
                throw RegraNegocioException("A data de nascimento não pode estar no futuro.")
            }
        }
        animal.nome = request.nome.trim()
        animal.especie = request.especie.trim()
        animal.raca = request.raca?.trim()?.takeIf { it.isNotEmpty() }
        animal.sexo = request.sexo?.trim()?.takeIf { it.isNotEmpty() }
        animal.dataNascimento = request.dataNascimento
        animal.tutor = tutor
    }

    private fun obter(id: Long): Animal =
        repository.findById(id).orElseThrow { RecursoNaoEncontradoException("Animal não encontrado.") }
}
