package br.ufba.vetcare.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * Trilha de auditoria imutável (ADR-05). Registra quem fez o quê, quando e de qual IP.
 */
@Entity
@Table(name = "auditoria")
class Auditoria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "usuario_email", length = 160)
    var usuarioEmail: String? = null,

    @Column(nullable = false, length = 60)
    var acao: String = "",

    @Column(length = 60)
    var entidade: String? = null,

    @Column(name = "entidade_id")
    var entidadeId: Long? = null,

    @Column(length = 500)
    var detalhes: String? = null,

    @Column(length = 45)
    var ip: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),
)
