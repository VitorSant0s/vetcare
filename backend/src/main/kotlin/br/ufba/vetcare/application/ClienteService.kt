package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AnimalRequest
import br.ufba.vetcare.api.dto.AnimalResposta
import br.ufba.vetcare.api.dto.AtendimentoResposta
import br.ufba.vetcare.api.dto.ConsultaResposta
import br.ufba.vetcare.api.dto.MeusDadosResposta
import br.ufba.vetcare.api.dto.NotificacaoResposta
import br.ufba.vetcare.api.dto.PagamentoResposta
import br.ufba.vetcare.api.dto.SolicitacaoRequest
import br.ufba.vetcare.api.dto.SolicitacaoResposta
import br.ufba.vetcare.api.dto.TutorResposta
import br.ufba.vetcare.api.dto.VacinacaoResposta
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.SolicitacaoAgendamento
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.AtendimentoRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.NotificacaoRepository
import br.ufba.vetcare.domain.repository.PagamentoRepository
import br.ufba.vetcare.domain.repository.SolicitacaoAgendamentoRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import br.ufba.vetcare.domain.repository.VacinacaoRepository
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
    private val consultaRepository: ConsultaRepository,
    private val atendimentoRepository: AtendimentoRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val pagamentoRepository: PagamentoRepository,
    private val notificacaoRepository: NotificacaoRepository,
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

    @Transactional(readOnly = true)
    fun exportarDados(tutorId: Long): MeusDadosResposta {
        val tutor = tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Cliente não encontrado.") }
        val pets = animalRepository.findAllByTutorIdAndAtivoTrue(tutorId)
        val animalIds = pets.mapNotNull { it.id }
        val resposta = MeusDadosResposta(
            tutor = TutorResposta.de(tutor),
            pets = pets.map(AnimalResposta::de),
            consultas = consultaRepository.findByTutorIdOrderByDataHoraDesc(tutorId).map(ConsultaResposta::de),
            atendimentos = if (animalIds.isEmpty()) emptyList() else
                atendimentoRepository.findByAnimalIdInOrderByDataHoraDesc(animalIds).map(AtendimentoResposta::de),
            vacinacoes = if (animalIds.isEmpty()) emptyList() else
                vacinacaoRepository.findByAnimalIdInOrderByDataAplicacaoDesc(animalIds).map(VacinacaoResposta::de),
            pagamentos = pagamentoRepository.findByTutorIdOrderByDataPagamentoDesc(tutorId)
                .map(PagamentoResposta::de),
            solicitacoes = solicitacaoRepository.findByTutorIdOrderByCriadoEmDesc(tutorId)
                .map(SolicitacaoResposta::de),
            notificacoes = notificacaoRepository.findByTutorIdOrderByCriadoEmDesc(tutorId)
                .map(NotificacaoResposta::de),
        )
        auditoriaService.registrarIndependente(
            acao = "LGPD_DADOS_EXPORTADOS",
            entidade = "Tutor",
            entidadeId = tutorId,
            usuarioEmail = tutor.email,
        )
        return resposta
    }

    @Transactional
    fun solicitarExclusao(tutorId: Long) {
        val tutor = tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Cliente não encontrado.") }
        if (tutor.anonimizado) return
        val emailOriginal = tutor.email
        tutor.nome = "Titular anonimizado"
        tutor.cpf = "999" + tutorId.toString().padStart(8, '0')
        tutor.email = "anonimo+$tutorId@lgpd.local"
        tutor.telefone = ""
        tutor.endereco = null
        tutor.senha = null
        tutor.ativo = false
        tutor.anonimizado = true
        tutor.primeiroAcesso = false
        tutor.exclusaoSolicitadaEm = java.time.Instant.now()
        tutor.tokenVersion += 1
        tutorRepository.save(tutor)
        auditoriaService.registrar(
            acao = "LGPD_EXCLUSAO_SOLICITADA",
            entidade = "Tutor",
            entidadeId = tutorId,
            detalhes = "Dados pessoais anonimizados; prontuários clínicos preservados por obrigação legal.",
            usuarioEmail = emailOriginal,
        )
    }
}
