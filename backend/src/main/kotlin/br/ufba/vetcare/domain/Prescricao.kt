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

/**
 * REQ13 - Prescrição vinculada a um atendimento.
 */
@Entity
@Table(name = "prescricoes")
class Prescricao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    var atendimento: Atendimento? = null,

    @Column(nullable = false, length = 160)
    var medicamento: String = "",

    @Column(length = 120)
    var dosagem: String? = null,

    @Column(length = 120)
    var frequencia: String? = null,

    @Column(length = 120)
    var duracao: String? = null,

    @Column(length = 500)
    var orientacoes: String? = null,
)
