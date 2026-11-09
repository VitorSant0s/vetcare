package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.NotificacaoResposta
import br.ufba.vetcare.api.dto.ProcessamentoNotificacoesResposta
import br.ufba.vetcare.application.NotificacaoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notificacoes")
class NotificacaoController(private val service: NotificacaoService) {

    @GetMapping
    fun listar(): List<NotificacaoResposta> = service.listar()

    @PostMapping("/processar")
    fun processar(): ProcessamentoNotificacoesResposta = service.processarTudo()
}
