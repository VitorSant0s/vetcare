package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.RelatorioResumo
import br.ufba.vetcare.application.RelatorioService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/relatorios")
class RelatorioController(private val service: RelatorioService) {

    @GetMapping("/resumo")
    fun resumo(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) inicio: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fim: LocalDate?,
    ): RelatorioResumo = service.resumo(inicio, fim)
}
