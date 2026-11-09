package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.ProntuarioResposta
import br.ufba.vetcare.application.ProntuarioService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/animais")
class ProntuarioController(private val service: ProntuarioService) {

    @GetMapping("/{animalId}/prontuario")
    fun prontuario(@PathVariable animalId: Long): ProntuarioResposta = service.montar(animalId)
}
