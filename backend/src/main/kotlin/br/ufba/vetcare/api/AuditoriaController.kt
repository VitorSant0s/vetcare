package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.AuditoriaResposta
import br.ufba.vetcare.api.dto.PageResponse
import br.ufba.vetcare.application.AuditoriaConsultaService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/auditoria")
class AuditoriaController(private val service: AuditoriaConsultaService) {

    @GetMapping
    fun listar(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) inicio: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fim: LocalDate?,
        @RequestParam(required = false) acao: String?,
        @RequestParam(required = false) usuario: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "30") size: Int,
    ): PageResponse<AuditoriaResposta> = service.listar(inicio, fim, acao, usuario, page, size)
}
