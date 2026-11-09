package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.AtendimentoRequest
import br.ufba.vetcare.api.dto.AtendimentoResposta
import br.ufba.vetcare.application.AtendimentoService
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AtendimentoController(private val service: AtendimentoService) {

    @PostMapping("/consultas/{consultaId}/atendimento")
    @ResponseStatus(HttpStatus.CREATED)
    fun registrar(
        @PathVariable consultaId: Long,
        @Valid @RequestBody request: AtendimentoRequest,
        @AuthenticationPrincipal solicitante: UsuarioAutenticado,
    ): AtendimentoResposta = service.registrar(consultaId, request, solicitante)

    @GetMapping("/atendimentos/{id}")
    fun buscar(@PathVariable id: Long): AtendimentoResposta = service.buscar(id)
}
