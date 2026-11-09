package br.ufba.vetcare.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDateTime

/**
 * REQ10 - Atendimento realizado pelo veterinário, base do prontuário (REQ11).
 */
@Entity
@Table(name = "atendimentos")
class Atendimento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false, unique = true)
    var consulta: Consulta = Consulta(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    var animal: Animal = Animal(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    var veterinario: Usuario = Usuario(),

    @Column(name = "data_hora", nullable = false)
    var dataHora: LocalDateTime = LocalDateTime.now(),

    @Column(length = 2000)
    var anamnese: String? = null,

    @Column(nullable = false, length = 2000)
    var diagnostico: String = "",

    @Column(length = 2000)
    var procedimentos: String? = null,

    @Column(length = 2000)
    var observacoes: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),

    @OneToMany(mappedBy = "atendimento", cascade = [CascadeType.ALL], orphanRemoval = true)
    var prescricoes: MutableList<Prescricao> = mutableListOf(),
)
