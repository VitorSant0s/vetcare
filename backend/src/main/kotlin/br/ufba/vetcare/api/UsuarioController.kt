package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.api.dto.UsuarioRequest
import br.ufba.vetcare.api.dto.UsuarioResposta
import br.ufba.vetcare.application.UsuarioService
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(private val service: UsuarioService) {

    @GetMapping
    fun listar(
        @RequestParam(required = false) termo: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<UsuarioResposta> = service.listar(termo, page, size)

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long): UsuarioResposta = service.buscar(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody request: UsuarioRequest): UsuarioResposta = service.criar(request)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: UsuarioRequest,
        @AuthenticationPrincipal solicitante: UsuarioAutenticado,
    ): UsuarioResposta = service.atualizar(id, request, solicitante.id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long, @AuthenticationPrincipal solicitante: UsuarioAutenticado) =
        service.excluir(id, solicitante.id)
}
