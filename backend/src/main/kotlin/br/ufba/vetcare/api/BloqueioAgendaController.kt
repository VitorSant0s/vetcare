package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.BloqueioAgendaRequest
import br.ufba.vetcare.api.dto.BloqueioAgendaResposta
import br.ufba.vetcare.application.BloqueioAgendaService
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/veterinarios/me/bloqueios")
class BloqueioAgendaController(private val service: BloqueioAgendaService) {

    @GetMapping
    fun listar(
        @AuthenticationPrincipal veterinario: UsuarioAutenticado,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) inicio: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fim: LocalDate?,
    ): List<BloqueioAgendaResposta> = service.listar(veterinario.id, inicio, fim)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun bloquear(
        @AuthenticationPrincipal veterinario: UsuarioAutenticado,
        @Valid @RequestBody request: BloqueioAgendaRequest,
    ): BloqueioAgendaResposta = service.bloquear(veterinario.id, request)

    @DeleteMapping("/{data}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun desbloquear(
        @AuthenticationPrincipal veterinario: UsuarioAutenticado,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) data: LocalDate,
    ) = service.desbloquear(veterinario.id, data)
}
