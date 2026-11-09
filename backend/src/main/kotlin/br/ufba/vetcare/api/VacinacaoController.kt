package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.VacinacaoRequest
import br.ufba.vetcare.api.dto.VacinacaoResposta
import br.ufba.vetcare.application.VacinacaoService
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class VacinacaoController(private val service: VacinacaoService) {

    @GetMapping("/animais/{animalId}/vacinacoes")
    fun listar(@PathVariable animalId: Long): List<VacinacaoResposta> = service.listar(animalId)

    @PostMapping("/animais/{animalId}/vacinacoes")
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @PathVariable animalId: Long,
        @Valid @RequestBody request: VacinacaoRequest,
        @AuthenticationPrincipal solicitante: UsuarioAutenticado,
    ): VacinacaoResposta = service.criar(animalId, request, solicitante)

    @DeleteMapping("/vacinacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) = service.excluir(id)
}
