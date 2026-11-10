package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AuditoriaResposta
import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.api.dto.toPageResponse
import br.ufba.vetcare.domain.repository.AuditoriaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

/**
 * Consulta da trilha de auditoria, restrita ao administrador.
 */
@Service
class AuditoriaConsultaService(
    private val repository: AuditoriaRepository,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional
    fun listar(
        inicio: LocalDate?,
        fim: LocalDate?,
        acao: String?,
        usuario: String?,
        page: Int,
        size: Int,
    ): PageResponse<AuditoriaResposta> {
        val zona = ZoneId.systemDefault()
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100))
        val resultado = repository.buscar(
            inicio = (inicio ?: LocalDate.of(2000, 1, 1)).atStartOfDay(zona).toInstant(),
            fim = (fim ?: LocalDate.now()).plusDays(1).atStartOfDay(zona).toInstant(),
            acao = acao?.trim() ?: "",
            usuario = usuario?.trim() ?: "",
            pageable = pageable,
        )
        auditoriaService.registrarIndependente(
            acao = "AUDITORIA_CONSULTADA",
            entidade = "Auditoria",
            detalhes = "Filtros: período ${inicio ?: '-'} a ${fim ?: '-'}, ação ${acao ?: '-'}, usuário ${usuario ?: '-'}",
        )
        return resultado.map(AuditoriaResposta::de).toPageResponse()
    }
}
