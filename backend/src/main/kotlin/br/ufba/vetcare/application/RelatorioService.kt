package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.RelatorioResumo
import br.ufba.vetcare.domain.StatusConsulta
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.AtendimentoRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.VacinacaoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

/**
 * REQ17 - Relatório gerencial consolidado por período.
 */
@Service
class RelatorioService(
    private val consultaRepository: ConsultaRepository,
    private val atendimentoRepository: AtendimentoRepository,
    private val animalRepository: AnimalRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val pagamentoService: PagamentoService,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun resumo(inicio: LocalDate?, fim: LocalDate?): RelatorioResumo {
        val periodo = pagamentoService.periodo(inicio, fim)
        val dataInicio = periodo.first
        val dataFim = periodo.second
        val consultas = consultaRepository.findByDataHoraBetween(
            dataInicio.atStartOfDay(),
            dataFim.plusDays(1).atStartOfDay(),
        )
        val financeiro = pagamentoService.dashboard(dataInicio, dataFim)
        val zona = ZoneId.systemDefault()

        auditoriaService.registrarIndependente(
            acao = "RELATORIO_GERADO",
            entidade = "Relatorio",
            detalhes = "Período $dataInicio a $dataFim",
        )
        return RelatorioResumo(
            inicio = dataInicio,
            fim = dataFim,
            consultasTotal = consultas.size,
            consultasAgendadas = consultas.count { it.status == StatusConsulta.AGENDADA },
            consultasRealizadas = consultas.count { it.status == StatusConsulta.REALIZADA },
            consultasCanceladas = consultas.count { it.status == StatusConsulta.CANCELADA },
            atendimentos = atendimentoRepository.countByDataHoraBetween(
                dataInicio.atStartOfDay(),
                dataFim.plusDays(1).atStartOfDay(),
            ),
            animaisCadastrados = animalRepository.countByAtivoTrue(),
            animaisNovos = animalRepository.countByCriadoEmBetween(
                dataInicio.atStartOfDay(zona).toInstant(),
                dataFim.plusDays(1).atStartOfDay(zona).toInstant(),
            ),
            vacinasAplicadas = vacinacaoRepository.countByDataAplicacaoBetween(dataInicio, dataFim),
            totalRecebido = financeiro.totalRecebido,
            porForma = financeiro.porForma,
        )
    }
}
