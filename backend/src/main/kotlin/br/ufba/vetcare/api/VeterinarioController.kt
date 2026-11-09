package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.VeterinarioRequest
import br.ufba.vetcare.api.dto.VeterinarioResposta
import br.ufba.vetcare.application.VeterinarioService
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
@RequestMapping("/api/veterinarios")
class VeterinarioController(private val service: VeterinarioService) {

    @GetMapping
    fun listar(
        @RequestParam(defaultValue = "false") incluirInativos: Boolean,
        @AuthenticationPrincipal usuario: UsuarioAutenticado,
    ): List<VeterinarioResposta> =
        service.listar(incluirInativos && usuario.perfil == Perfil.ADMINISTRADOR)

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long): VeterinarioResposta = service.buscar(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody request: VeterinarioRequest): VeterinarioResposta = service.criar(request)

    @PutMapping("/{id}")
    fun atualizar(@PathVariable id: Long, @Valid @RequestBody request: VeterinarioRequest): VeterinarioResposta =
        service.atualizar(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) = service.excluir(id)
}
