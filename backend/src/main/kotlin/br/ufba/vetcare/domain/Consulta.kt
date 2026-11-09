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

@Entity
@Table(name = "consultas")
class Consulta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    var animal: Animal = Animal(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    var tutor: Tutor = Tutor(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    var veterinario: Usuario = Usuario(),

    @Column(name = "data_hora", nullable = false)
    var dataHora: LocalDateTime = LocalDateTime.now(),

    /**
     * Preenchido apenas enquanto a consulta ocupa o horário do veterinário.
     * Ao cancelar, vira null, liberando o slot para um novo agendamento.
     */
    @Column(name = "horario_ativo")
    var horarioAtivo: LocalDateTime? = dataHora,

    @Column(name = "motivo_cancelamento", length = 300)
    var motivoCancelamento: String? = null,

    @Column(name = "cancelada_em")
    var canceladaEm: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StatusConsulta = StatusConsulta.AGENDADA,

    @Column(length = 500)
    var observacoes: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),
)
