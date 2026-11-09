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
 * REQ12 - Vacina aplicada no animal, com previsão da próxima dose.
 */
@Entity
@Table(name = "vacinacoes")
class Vacinacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    var animal: Animal = Animal(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id")
    var veterinario: Usuario? = null,

    @Column(name = "tipo_vacina", nullable = false, length = 120)
    var tipoVacina: String = "",

    @Column(length = 60)
    var lote: String? = null,

    @Column(name = "data_aplicacao", nullable = false)
    var dataAplicacao: LocalDate = LocalDate.now(),

    @Column(name = "proxima_dose")
    var proximaDose: LocalDate? = null,

    @Column(length = 500)
    var observacoes: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),
)
