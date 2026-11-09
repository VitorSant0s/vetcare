package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.UsuarioResposta
import br.ufba.vetcare.application.UsuarioService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Lista de veterinários ativos usada no agendamento (REQ07). Disponível a todos os perfis autenticados.
 */
@RestController
@RequestMapping("/api/veterinarios")
class VeterinarioController(private val service: UsuarioService) {

    @GetMapping
    fun listar(): List<UsuarioResposta> = service.veterinarios()
}
