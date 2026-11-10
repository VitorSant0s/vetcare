package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.AnimalRequest
import br.ufba.vetcare.api.dto.AnimalResposta
import br.ufba.vetcare.api.dto.MeusDadosResposta
import br.ufba.vetcare.api.dto.SolicitacaoRequest
import br.ufba.vetcare.api.dto.SolicitacaoResposta
import br.ufba.vetcare.application.ClienteService
import br.ufba.vetcare.infrastructure.security.TutorAutenticado
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cliente")
class ClienteController(private val service: ClienteService) {

    @GetMapping("/pets")
    fun listarPets(@AuthenticationPrincipal tutor: TutorAutenticado): List<AnimalResposta> =
        service.listarPets(tutor.id)

    @PostMapping("/pets")
    @ResponseStatus(HttpStatus.CREATED)
    fun criarPet(
        @AuthenticationPrincipal tutor: TutorAutenticado,
        @Valid @RequestBody request: AnimalRequest,
    ): AnimalResposta = service.criarPet(tutor.id, request)

    @PutMapping("/pets/{id}")
    fun atualizarPet(
        @AuthenticationPrincipal tutor: TutorAutenticado,
        @PathVariable id: Long,
        @Valid @RequestBody request: AnimalRequest,
    ): AnimalResposta = service.atualizarPet(tutor.id, id, request)

    @GetMapping("/meus-dados")
    fun meusDados(@AuthenticationPrincipal tutor: TutorAutenticado): MeusDadosResposta =
        service.exportarDados(tutor.id)

    @DeleteMapping("/conta")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluirConta(@AuthenticationPrincipal tutor: TutorAutenticado) =
        service.solicitarExclusao(tutor.id)

    @GetMapping("/solicitacoes")
    fun listarSolicitacoes(@AuthenticationPrincipal tutor: TutorAutenticado): List<SolicitacaoResposta> =
        service.listarSolicitacoes(tutor.id)

    @PostMapping("/solicitacoes")
    @ResponseStatus(HttpStatus.CREATED)
    fun solicitar(
        @AuthenticationPrincipal tutor: TutorAutenticado,
        @Valid @RequestBody request: SolicitacaoRequest,
    ): SolicitacaoResposta = service.solicitar(tutor.id, request)
}
