package br.ufba.vetcare.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

/**
 * Dia de trabalho bloqueado pelo veterinário (férias, congresso, atestado etc.).
 */
@Entity
@Table(name = "bloqueios_agenda")
class BloqueioAgenda(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    var veterinario: Usuario = Usuario(),

    @Column(nullable = false)
    var data: LocalDate = LocalDate.now(),

    @Column(nullable = false, length = 500)
    var motivo: String = "",

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),
)
