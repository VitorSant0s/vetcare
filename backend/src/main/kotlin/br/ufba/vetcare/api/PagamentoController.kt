package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.DashboardFinanceiro
import br.ufba.vetcare.api.dto.PagamentoRequest
import br.ufba.vetcare.api.dto.PagamentoResposta
import br.ufba.vetcare.application.PagamentoService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api")
class PagamentoController(private val service: PagamentoService) {

    @GetMapping("/pagamentos")
    fun listar(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) inicio: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fim: LocalDate?,
    ): List<PagamentoResposta> = service.listar(inicio, fim)

    @PostMapping("/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody request: PagamentoRequest): PagamentoResposta = service.criar(request)

    @GetMapping("/financeiro/dashboard")
    fun dashboard(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) inicio: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fim: LocalDate?,
    ): DashboardFinanceiro = service.dashboard(inicio, fim)
}
