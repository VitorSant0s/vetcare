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
import java.time.LocalDateTime

enum class StatusSolicitacao {
    PENDENTE,
    APROVADA,
    RECUSADA,
    CANCELADA,
}

/**
 * Pedido de agendamento feito pelo cliente no portal, sujeito à aprovação da recepção.
 */
@Entity
@Table(name = "solicitacoes_agendamento")
class SolicitacaoAgendamento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    var tutor: Tutor = Tutor(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    var animal: Animal = Animal(),

    @Column(name = "data_preferida", nullable = false)
    var dataPreferida: LocalDateTime = LocalDateTime.now(),

    @Column(length = 500)
    var motivo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StatusSolicitacao = StatusSolicitacao.PENDENTE,

    @Column(name = "observacao_resposta", length = 500)
    var observacaoResposta: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    var consulta: Consulta? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),

    @Column(name = "analisado_em")
    var analisadoEm: Instant? = null,
)
