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

/**
 * Código temporário que permite a outro veterinário consultar o prontuário do animal.
 */
@Entity
@Table(name = "compartilhamentos_prontuario")
class CompartilhamentoProntuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    var animal: Animal = Animal(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por", nullable = false)
    var criadoPor: Usuario = Usuario(),

    @Column(nullable = false, length = 12, unique = true)
    var codigo: String = "",

    @Column(name = "expira_em", nullable = false)
    var expiraEm: Instant = Instant.now(),

    @Column(nullable = false)
    var ativo: Boolean = true,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),
)
