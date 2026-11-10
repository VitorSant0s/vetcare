package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.DashboardFinanceiro
import br.ufba.vetcare.api.dto.FormaTotal
import br.ufba.vetcare.api.dto.PagamentoRequest
import br.ufba.vetcare.api.dto.PagamentoResposta
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.FormaPagamento
import br.ufba.vetcare.domain.Pagamento
import br.ufba.vetcare.domain.StatusPagamento
import br.ufba.vetcare.domain.repository.AtendimentoRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.PagamentoRepository
import br.ufba.vetcare.domain.repository.TutorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * REQ14 - Registrar Pagamento e REQ15 - Dashboard Financeiro.
 */
@Service
class PagamentoService(
    private val repository: PagamentoRepository,
    private val tutorRepository: TutorRepository,
    private val consultaRepository: ConsultaRepository,
    private val atendimentoRepository: AtendimentoRepository,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun listar(inicio: LocalDate?, fim: LocalDate?): List<PagamentoResposta> {
        val periodo = periodo(inicio, fim)
        return repository.findByDataPagamentoBetweenOrderByDataPagamentoDesc(periodo.first, periodo.second)
            .map(PagamentoResposta::de)
    }

    @Transactional
    fun criar(request: PagamentoRequest): PagamentoResposta {
        val tutorId = request.tutorId ?: throw RegraNegocioException("Selecione o tutor.")
        val tutor = tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Tutor não encontrado.") }
        val consulta = request.consultaId?.let { consultaId ->
            val encontrada = consultaRepository.findById(consultaId)
                .orElseThrow { RecursoNaoEncontradoException("Consulta não encontrada.") }
            if (encontrada.tutor.id != tutorId) {
                throw RegraNegocioException("A consulta selecionada não pertence ao tutor informado.")
            }
            encontrada
        }
        val valor = request.valor ?: throw RegraNegocioException("Informe o valor.")
        if (valor <= BigDecimal.ZERO) {
            throw RegraNegocioException("O valor deve ser maior que zero.")
        }
        val data = request.dataPagamento ?: throw RegraNegocioException("Informe a data do pagamento.")
        if (data.isAfter(LocalDate.now())) {
            throw RegraNegocioException("A data do pagamento não pode estar no futuro.")
        }
        val pagamento = repository.save(
            Pagamento(
                tutor = tutor,
                consulta = consulta,
                valor = valor.setScale(2, RoundingMode.HALF_UP),
                formaPagamento = request.formaPagamento ?: FormaPagamento.PIX,
                status = request.status ?: StatusPagamento.PAGO,
                dataPagamento = data,
                observacoes = request.observacoes?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
        auditoriaService.registrar(
            acao = "PAGAMENTO_REGISTRADO",
            entidade = "Pagamento",
            entidadeId = pagamento.id,
            detalhes = "R$ ${pagamento.valor} · ${pagamento.formaPagamento.descricao} · ${tutor.nome}",
        )
        return PagamentoResposta.de(pagamento)
    }

    @Transactional(readOnly = true)
    fun dashboard(inicio: LocalDate?, fim: LocalDate?): DashboardFinanceiro {
        val periodo = periodo(inicio, fim)
        val pagamentos = repository.findByDataPagamentoBetweenOrderByDataPagamentoDesc(periodo.first, periodo.second)
        val recebidos = pagamentos.filter { it.status == StatusPagamento.PAGO }
        val total = recebidos.fold(BigDecimal.ZERO) { soma, pagamento -> soma + pagamento.valor }
        val quantidade = recebidos.size
        val atendimentos = atendimentoRepository.countByDataHoraBetween(
            periodo.first.atStartOfDay(),
            periodo.second.plusDays(1).atStartOfDay(),
        )
        val porForma = FormaPagamento.entries.map { forma ->
            val doTipo = recebidos.filter { it.formaPagamento == forma }
            FormaTotal(
                forma = forma.name,
                descricao = forma.descricao,
                total = doTipo.fold(BigDecimal.ZERO) { soma, pagamento -> soma + pagamento.valor },
                quantidade = doTipo.size,
            )
        }.filter { it.quantidade > 0 }

        auditoriaService.registrarIndependente(
            acao = "DASHBOARD_FINANCEIRO_ACESSADO",
            entidade = "Pagamento",
            detalhes = "Período ${periodo.first} a ${periodo.second}",
        )
        return DashboardFinanceiro(
            inicio = periodo.first,
            fim = periodo.second,
            totalRecebido = total,
            quantidadePagamentos = quantidade,
            ticketMedio = if (quantidade == 0) BigDecimal.ZERO else total.divide(
                BigDecimal(quantidade),
                2,
                RoundingMode.HALF_UP,
            ),
            atendimentos = atendimentos,
            porForma = porForma,
        )
    }

    fun periodo(inicio: LocalDate?, fim: LocalDate?): Pair<LocalDate, LocalDate> {
        val dataFim = fim ?: LocalDate.now()
        val dataInicio = inicio ?: dataFim.withDayOfMonth(1)
        if (dataFim.isBefore(dataInicio)) {
            throw RegraNegocioException("A data final deve ser posterior à inicial.")
        }
        return dataInicio to dataFim
    }
}
