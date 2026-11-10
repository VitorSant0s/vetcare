package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.LoginRequest
import br.ufba.vetcare.api.dto.LoginResponse
import br.ufba.vetcare.api.dto.UsuarioResposta
import br.ufba.vetcare.application.AuthService
import br.ufba.vetcare.infrastructure.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse = authService.login(request)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal usuario: UsuarioAutenticado): UsuarioResposta =
        authService.usuarioAtual(usuario.email)

    @PostMapping("/logout")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    fun logout(@AuthenticationPrincipal usuario: UsuarioAutenticado) = authService.logout(usuario.email)
}
