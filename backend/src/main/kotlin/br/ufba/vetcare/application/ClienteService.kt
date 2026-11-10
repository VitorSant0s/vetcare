package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AnimalRequest
import br.ufba.vetcare.api.dto.AnimalResposta
import br.ufba.vetcare.api.dto.SolicitacaoRequest
import br.ufba.vetcare.api.dto.SolicitacaoResposta
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.SolicitacaoAgendamento
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.SolicitacaoAgendamentoRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Operações do cliente autenticado: seus pets e seus pedidos de agendamento.
 */
@Service
class ClienteService(
    private val tutorRepository: TutorRepository,
    private val animalRepository: AnimalRepository,
    private val solicitacaoRepository: SolicitacaoAgendamentoRepository,
    private val animalService: AnimalService,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listarPets(tutorId: Long): List<AnimalResposta> {
        if (!tutorRepository.existsById(tutorId)) {
            throw RecursoNaoEncontradoException("Cliente não encontrado.")
        }
        return animalRepository.findAllByTutorIdAndAtivoTrue(tutorId).map(AnimalResposta::de)
    }

    @Transactional
    fun criarPet(tutorId: Long, request: AnimalRequest): AnimalResposta {
        val tutor = tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Cliente não encontrado.") }
        if (!tutor.ativo) {
            throw RegraNegocioException("Cadastro inativo. Procure a clínica.")
        }
        return animalService.criar(request.copy(tutorId = tutorId))
    }

    @Transactional
    fun atualizarPet(tutorId: Long, animalId: Long, request: AnimalRequest): AnimalResposta {
        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Pet não encontrado.") }
        if (animal.tutor.id != tutorId) {
            throw RecursoNaoEncontradoException("Pet não encontrado.")
        }
        return animalService.atualizar(animalId, request.copy(tutorId = tutorId))
    }

    @Transactional(readOnly = true)
    fun listarSolicitacoes(tutorId: Long): List<SolicitacaoResposta> =
        solicitacaoRepository.findByTutorIdOrderByCriadoEmDesc(tutorId).map(SolicitacaoResposta::de)

    @Transactional
    fun solicitar(tutorId: Long, request: SolicitacaoRequest): SolicitacaoResposta {
        val tutor = tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Cliente não encontrado.") }
        val animalId = request.animalId ?: throw RegraNegocioException("Selecione o pet.")
        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Pet não encontrado.") }
        if (animal.tutor.id != tutorId || !animal.ativo) {
            throw RegraNegocioException("O pet selecionado não pertence ao seu cadastro.")
        }
        val data = request.dataPreferida ?: throw RegraNegocioException("Informe a data e o horário preferidos.")
        if (!data.isAfter(LocalDateTime.now())) {
            throw RegraNegocioException("A data preferida deve estar no futuro.")
        }
        val solicitacao = solicitacaoRepository.save(
            SolicitacaoAgendamento(
                tutor = tutor,
                animal = animal,
                dataPreferida = data,
                motivo = request.motivo?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
        auditoriaService.registrar(
            acao = "SOLICITACAO_CRIADA",
            entidade = "SolicitacaoAgendamento",
            entidadeId = solicitacao.id,
            detalhes = "${animal.nome} em $data",
            usuarioEmail = tutor.email,
        )
        return SolicitacaoResposta.de(solicitacao)
    }
}
