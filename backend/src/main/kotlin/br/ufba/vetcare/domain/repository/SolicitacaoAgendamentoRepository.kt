package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.SolicitacaoAgendamento
import br.ufba.vetcare.domain.StatusSolicitacao
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface SolicitacaoAgendamentoRepository : JpaRepository<SolicitacaoAgendamento, Long> {
    @EntityGraph(attributePaths = ["tutor", "animal", "consulta"])
    fun findByTutorIdOrderByCriadoEmDesc(tutorId: Long): List<SolicitacaoAgendamento>

    @EntityGraph(attributePaths = ["tutor", "animal", "consulta"])
    fun findAllByOrderByCriadoEmDesc(): List<SolicitacaoAgendamento>

    @EntityGraph(attributePaths = ["tutor", "animal", "consulta"])
    fun findByStatusOrderByCriadoEm(status: StatusSolicitacao): List<SolicitacaoAgendamento>

    @EntityGraph(attributePaths = ["tutor", "animal", "consulta"])
    override fun findById(id: Long): java.util.Optional<SolicitacaoAgendamento>
}
