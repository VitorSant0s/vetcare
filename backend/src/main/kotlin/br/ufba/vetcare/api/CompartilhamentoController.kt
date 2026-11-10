package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.CompartilhamentoResposta
import br.ufba.vetcare.api.dto.ProntuarioResposta
import br.ufba.vetcare.application.CompartilhamentoService
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CompartilhamentoController(private val service: CompartilhamentoService) {

    @PostMapping("/animais/{animalId}/compartilhamento")
    @ResponseStatus(HttpStatus.CREATED)
    fun compartilhar(
        @PathVariable animalId: Long,
        @AuthenticationPrincipal veterinario: UsuarioAutenticado,
    ): CompartilhamentoResposta = service.compartilhar(animalId, veterinario.id)

    @DeleteMapping("/animais/{animalId}/compartilhamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revogar(
        @PathVariable animalId: Long,
        @AuthenticationPrincipal veterinario: UsuarioAutenticado,
    ) = service.revogar(animalId, veterinario.id)

    @GetMapping("/prontuarios/compartilhados/{codigo}")
    fun buscarPorCodigo(@PathVariable codigo: String): ProntuarioResposta = service.buscarPorCodigo(codigo)
}
