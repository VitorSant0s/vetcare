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
import java.time.Instant

/**
 * REQ16 - Aviso ao tutor sobre consulta ou vacinação, processado por canal externo.
 */
@Entity
@Table(name = "notificacoes")
class Notificacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    var tutor: Tutor = Tutor(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    var consulta: Consulta? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacinacao_id")
    var vacinacao: Vacinacao? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var tipo: TipoNotificacao = TipoNotificacao.CONSULTA,

    @Column(nullable = false, length = 20)
    var canal: String = "EMAIL",

    @Column(nullable = false, length = 160)
    var destinatario: String = "",

    @Column(nullable = false, length = 500)
    var mensagem: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StatusNotificacao = StatusNotificacao.PENDENTE,

    @Column(length = 300)
    var erro: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),

    @Column(name = "enviado_em")
    var enviadoEm: Instant? = null,
)
