package br.ufba.vetcare.domain

enum class FormaPagamento {
    DINHEIRO,
    PIX,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    CONVENIO;

    val descricao: String
        get() = when (this) {
            DINHEIRO -> "Dinheiro"
            PIX -> "Pix"
            CARTAO_CREDITO -> "Cartão de crédito"
            CARTAO_DEBITO -> "Cartão de débito"
            CONVENIO -> "Convênio"
        }
}

enum class StatusPagamento {
    PAGO,
    PENDENTE,
    CANCELADO,
}

enum class TipoNotificacao {
    CONSULTA,
    VACINA,
}

enum class StatusNotificacao {
    PENDENTE,
    ENVIADA,
    FALHA,
}
