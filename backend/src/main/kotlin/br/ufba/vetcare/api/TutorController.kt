package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.api.dto.TutorRequest
import br.ufba.vetcare.api.dto.TutorResposta
import br.ufba.vetcare.application.TutorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
@RequestMapping("/api/tutores")
class TutorController(private val service: TutorService) {

    @GetMapping
    fun listar(
        @RequestParam(required = false) termo: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<TutorResposta> = service.listar(termo, page, size)

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long): TutorResposta = service.buscar(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody request: TutorRequest): TutorResposta = service.criar(request)

    @PutMapping("/{id}")
    fun atualizar(@PathVariable id: Long, @Valid @RequestBody request: TutorRequest): TutorResposta =
        service.atualizar(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) = service.excluir(id)
}
