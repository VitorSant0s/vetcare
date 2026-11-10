package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.ConsultaRequest
import br.ufba.vetcare.api.dto.SolicitacaoDecisaoRequest
import br.ufba.vetcare.api.dto.SolicitacaoResposta
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.StatusSolicitacao
import br.ufba.vetcare.domain.repository.SolicitacaoAgendamentoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Aprovação/recusa dos pedidos de agendamento feitos pelo portal do cliente.
 */
@Service
class SolicitacaoService(
    private val repository: SolicitacaoAgendamentoRepository,
    private val consultaService: ConsultaService,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listar(status: StatusSolicitacao?): List<SolicitacaoResposta> {
        val lista = if (status != null) {
            repository.findByStatusOrderByCriadoEm(status)
        } else {
            repository.findAllByOrderByCriadoEmDesc()
        }
        return lista.map(SolicitacaoResposta::de)
    }

    @Transactional
    fun aprovar(id: Long, request: SolicitacaoDecisaoRequest): SolicitacaoResposta {
        val solicitacao = obterPendente(id)
        val veterinarioId = request.veterinarioId ?: throw RegraNegocioException("Selecione o veterinário.")
        val dataHora = request.dataHora ?: throw RegraNegocioException("Informe a data e o horário.")
        val consulta = consultaService.agendar(
            ConsultaRequest(
                animalId = solicitacao.animal.id,
                tutorId = solicitacao.tutor.id,
                veterinarioId = veterinarioId,
                dataHora = dataHora,
                observacoes = request.observacao?.trim()?.takeIf { it.isNotEmpty() }
                    ?: solicitacao.motivo,
            ),
        )
        solicitacao.status = StatusSolicitacao.APROVADA
        solicitacao.consulta = consultaService.referencia(consulta.id)
        solicitacao.observacaoResposta = request.observacao?.trim()?.takeIf { it.isNotEmpty() }
        solicitacao.analisadoEm = Instant.now()
        val salva = repository.save(solicitacao)
        auditoriaService.registrar(
            acao = "SOLICITACAO_APROVADA",
            entidade = "SolicitacaoAgendamento",
            entidadeId = id,
            detalhes = "Consulta ${consulta.id} gerada",
        )
        return SolicitacaoResposta.de(salva)
    }

    @Transactional
    fun recusar(id: Long, motivo: String): SolicitacaoResposta {
        val solicitacao = obterPendente(id)
        solicitacao.status = StatusSolicitacao.RECUSADA
        solicitacao.observacaoResposta = motivo.trim()
        solicitacao.analisadoEm = Instant.now()
        val salva = repository.save(solicitacao)
        auditoriaService.registrar(
            acao = "SOLICITACAO_RECUSADA",
            entidade = "SolicitacaoAgendamento",
            entidadeId = id,
            detalhes = motivo,
        )
        return SolicitacaoResposta.de(salva)
    }

    private fun obterPendente(id: Long) = repository.findById(id)
        .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
        .also {
            if (it.status != StatusSolicitacao.PENDENTE) {
                throw ConflitoException("Esta solicitação já foi analisada.")
            }
        }
}
