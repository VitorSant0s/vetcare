package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.AlterarSenhaRequest
import br.ufba.vetcare.api.dto.ConsentimentoRequest
import br.ufba.vetcare.api.dto.ClienteLoginRequest
import br.ufba.vetcare.api.dto.ClienteLoginResponse
import br.ufba.vetcare.api.dto.TutorResposta
import br.ufba.vetcare.application.ClienteAuthService
import br.ufba.vetcare.infrastructure.security.TutorAutenticado
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cliente/auth")
class ClienteAuthController(private val service: ClienteAuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: ClienteLoginRequest): ClienteLoginResponse = service.login(request)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal tutor: TutorAutenticado): TutorResposta = service.me(tutor.id)

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@AuthenticationPrincipal tutor: TutorAutenticado) = service.logout(tutor.id)

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun alterarSenha(
        @AuthenticationPrincipal tutor: TutorAutenticado,
        @Valid @RequestBody request: AlterarSenhaRequest,
    ) = service.alterarSenha(tutor.id, request)

    @PostMapping("/consentimento")
    fun consentir(
        @AuthenticationPrincipal tutor: TutorAutenticado,
        @Valid @RequestBody request: ConsentimentoRequest,
    ): TutorResposta = service.registrarConsentimento(tutor.id, request.versao)
}
