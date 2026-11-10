package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.ProntuarioResposta
import br.ufba.vetcare.application.ProntuarioService
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/animais")
class ProntuarioController(private val service: ProntuarioService) {

    @GetMapping("/{animalId}/prontuario")
    fun prontuario(
        @PathVariable animalId: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado,
    ): ProntuarioResposta = service.montar(
        animalId,
        if (usuario.perfil == Perfil.VETERINARIO) usuario.id else null,
    )
}
