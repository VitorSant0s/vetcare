package br.ufba.vetcare.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * REQ14 - Pagamento relacionado a uma consulta/procedimento realizado.
 */
@Entity
@Table(name = "pagamentos")
class Pagamento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    var tutor: Tutor = Tutor(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    var consulta: Consulta? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var valor: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    var formaPagamento: FormaPagamento = FormaPagamento.PIX,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StatusPagamento = StatusPagamento.PAGO,

    @Column(name = "data_pagamento", nullable = false)
    var dataPagamento: LocalDate = LocalDate.now(),

    @Column(length = 500)
    var observacoes: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),
)
