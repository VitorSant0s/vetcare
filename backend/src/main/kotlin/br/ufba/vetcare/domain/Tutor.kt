package br.ufba.vetcare.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "tutores")
class Tutor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 120)
    var nome: String = "",

    @Column(nullable = false, unique = true, length = 14)
    var cpf: String = "",

    @Column(nullable = false, length = 160)
    var email: String = "",

    @Column(nullable = false, length = 20)
    var telefone: String = "",

    @Column(length = 200)
    var endereco: String? = null,

    @Column(length = 100)
    var senha: String? = null,

    @Column(nullable = false)
    var ativo: Boolean = true,

    @Column(name = "primeiro_acesso", nullable = false)
    var primeiroAcesso: Boolean = true,

    @Column(name = "token_version", nullable = false)
    var tokenVersion: Int = 0,

    @Column(name = "consentimento_em")
    var consentimentoEm: Instant? = null,

    @Column(name = "consentimento_versao", length = 20)
    var consentimentoVersao: String? = null,

    @Column(nullable = false)
    var anonimizado: Boolean = false,

    @Column(name = "exclusao_solicitada_em")
    var exclusaoSolicitadaEm: Instant? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),

    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: Instant = Instant.now(),
)
