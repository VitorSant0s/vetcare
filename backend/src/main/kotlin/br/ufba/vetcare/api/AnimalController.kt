package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.AnimalRequest
import br.ufba.vetcare.api.dto.AnimalResposta
import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.application.AnimalService
import br.ufba.vetcare.domain.Perfil
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
@RequestMapping("/api/animais")
class AnimalController(private val service: AnimalService) {

    @GetMapping
    fun listar(
        @RequestParam(required = false) termo: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal usuario: UsuarioAutenticado,
    ): PageResponse<AnimalResposta> = service.listar(
        termo,
        page,
        size,
        veterinarioId = if (usuario.perfil == Perfil.VETERINARIO) usuario.id else null,
    )

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long, @AuthenticationPrincipal usuario: UsuarioAutenticado): AnimalResposta =
        service.buscar(id, if (usuario.perfil == Perfil.VETERINARIO) usuario.id else null)

    @GetMapping("/por-tutor/{tutorId}")
    fun listarPorTutor(@PathVariable tutorId: Long): List<AnimalResposta> = service.listarPorTutor(tutorId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody request: AnimalRequest): AnimalResposta = service.criar(request)

    @PutMapping("/{id}")
    fun atualizar(@PathVariable id: Long, @Valid @RequestBody request: AnimalRequest): AnimalResposta =
        service.atualizar(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) = service.excluir(id)
}
