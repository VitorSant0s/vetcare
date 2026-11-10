package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.CancelamentoRequest
import br.ufba.vetcare.api.dto.ConsultaRequest
import br.ufba.vetcare.api.dto.ConsultaResposta
import br.ufba.vetcare.application.ConsultaService
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/consultas")
class ConsultaController(private val service: ConsultaService) {

    /**
     * REQ07: listagem que comprova o agendamento. O veterinário enxerga apenas a própria agenda.
     */
    @GetMapping
    fun listarAgenda(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) inicio: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fim: LocalDate?,
        @RequestParam(required = false) veterinarioId: Long?,
        @AuthenticationPrincipal usuario: UsuarioAutenticado,
    ): List<ConsultaResposta> {
        val dataInicio = (inicio ?: LocalDate.now()).atStartOfDay()
        val dataFim = (fim ?: (inicio ?: LocalDate.now()).plusDays(7)).plusDays(1).atStartOfDay()
        val filtroVeterinario = if (usuario.perfil == Perfil.VETERINARIO) usuario.id else veterinarioId
        return service.listarAgenda(dataInicio, dataFim, filtroVeterinario)
    }

    @GetMapping("/{id}")
    fun buscar(
        @PathVariable id: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado,
    ): ConsultaResposta {
        val filtro = if (usuario.perfil == Perfil.VETERINARIO) usuario.id else null
        return service.buscar(id, filtro)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun agendar(@Valid @RequestBody request: ConsultaRequest): ConsultaResposta = service.agendar(request)

    @PostMapping("/{id}/cancelar")
    fun cancelar(
        @PathVariable id: Long,
        @Valid @RequestBody(required = false) request: CancelamentoRequest?,
        @AuthenticationPrincipal usuario: UsuarioAutenticado,
    ): ConsultaResposta = service.cancelar(id, request?.motivo, usuario)
}
