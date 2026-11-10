package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.SolicitacaoDecisaoRequest
import br.ufba.vetcare.api.dto.SolicitacaoRecusaRequest
import br.ufba.vetcare.api.dto.SolicitacaoResposta
import br.ufba.vetcare.application.SolicitacaoService
import br.ufba.vetcare.domain.StatusSolicitacao
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/solicitacoes")
class SolicitacaoController(private val service: SolicitacaoService) {

    @GetMapping
    fun listar(@RequestParam(required = false) status: StatusSolicitacao?): List<SolicitacaoResposta> =
        service.listar(status)

    @PostMapping("/{id}/aprovar")
    fun aprovar(
        @PathVariable id: Long,
        @Valid @RequestBody request: SolicitacaoDecisaoRequest,
    ): SolicitacaoResposta = service.aprovar(id, request)

    @PostMapping("/{id}/recusar")
    fun recusar(
        @PathVariable id: Long,
        @Valid @RequestBody request: SolicitacaoRecusaRequest,
    ): SolicitacaoResposta = service.recusar(id, request.motivo)
}
