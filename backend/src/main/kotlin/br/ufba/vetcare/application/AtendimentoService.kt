package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AtendimentoRequest
import br.ufba.vetcare.api.dto.AtendimentoResposta
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Atendimento
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Prescricao
import br.ufba.vetcare.domain.StatusConsulta
import br.ufba.vetcare.domain.repository.AtendimentoRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.UsuarioRepository
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import org.springframework.cache.annotation.CacheEvict
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * REQ10 - Registrar Atendimento. Ao registrar, a consulta passa a REALIZADA e o
 * registro compõe o prontuário do animal (REQ11).
 */
@Service
class AtendimentoService(
    private val repository: AtendimentoRepository,
    private val consultaRepository: ConsultaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional
    @CacheEvict(cacheNames = ["agenda"], allEntries = true)
    fun registrar(
        consultaId: Long,
        request: AtendimentoRequest,
        solicitante: UsuarioAutenticado,
    ): AtendimentoResposta {
        val consulta = consultaRepository.findById(consultaId)
            .orElseThrow { RecursoNaoEncontradoException("Consulta não encontrada.") }

        if (consulta.status == StatusConsulta.CANCELADA) {
            throw RegraNegocioException("Não é possível registrar atendimento de uma consulta cancelada.")
        }
        if (consulta.status == StatusConsulta.REALIZADA || repository.existsByConsultaId(consultaId)) {
            throw ConflitoException("Esta consulta já possui atendimento registrado.")
        }
        if (solicitante.perfil == Perfil.VETERINARIO && consulta.veterinario.id != solicitante.id) {
            throw AccessDeniedException("Apenas o veterinário responsável pode registrar este atendimento.")
        }
        if (consulta.dataHora.isAfter(LocalDateTime.now().plusMinutes(30))) {
            throw RegraNegocioException("O atendimento só pode ser registrado a partir do horário da consulta.")
        }

        val atendimento = Atendimento(
            consulta = consulta,
            animal = consulta.animal,
            veterinario = consulta.veterinario,
            dataHora = consulta.dataHora,
            anamnese = request.anamnese?.trim()?.takeIf { it.isNotEmpty() },
            diagnostico = request.diagnostico.trim(),
            procedimentos = request.procedimentos?.trim()?.takeIf { it.isNotEmpty() },
            observacoes = request.observacoes?.trim()?.takeIf { it.isNotEmpty() },
        )
        atendimento.prescricoes = request.prescricoes.map { prescricao ->
            Prescricao(
                atendimento = atendimento,
                medicamento = prescricao.medicamento.trim(),
                dosagem = prescricao.dosagem?.trim()?.takeIf { it.isNotEmpty() },
                frequencia = prescricao.frequencia?.trim()?.takeIf { it.isNotEmpty() },
                duracao = prescricao.duracao?.trim()?.takeIf { it.isNotEmpty() },
                orientacoes = prescricao.orientacoes?.trim()?.takeIf { it.isNotEmpty() },
            )
        }.toMutableList()

        val salvo = repository.save(atendimento)
        consulta.status = StatusConsulta.REALIZADA
        consultaRepository.save(consulta)

        auditoriaService.registrar(
            acao = "ATENDIMENTO_REGISTRADO",
            entidade = "Atendimento",
            entidadeId = salvo.id,
            detalhes = "Consulta ${consulta.id} do animal ${consulta.animal.nome}",
        )
        return AtendimentoResposta.de(salvo)
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): AtendimentoResposta =
        AtendimentoResposta.de(
            repository.findById(id).orElseThrow { RecursoNaoEncontradoException("Atendimento não encontrado.") },
        )
}
