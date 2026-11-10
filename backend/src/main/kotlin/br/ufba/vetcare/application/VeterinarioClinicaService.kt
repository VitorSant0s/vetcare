package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AtendimentoResposta
import br.ufba.vetcare.api.dto.PacienteClinicoResposta
import br.ufba.vetcare.domain.Animal
import br.ufba.vetcare.domain.repository.AtendimentoRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.VacinacaoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * Visão clínica do veterinário: apenas pacientes que já tiveram consulta com ele.
 */
@Service
class VeterinarioClinicaService(
    private val consultaRepository: ConsultaRepository,
    private val atendimentoRepository: AtendimentoRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional(readOnly = true)
    fun meusPacientes(veterinarioId: Long): List<PacienteClinicoResposta> {
        auditoriaService.registrarIndependente(
            acao = "PACIENTES_CLINICOS_CONSULTADOS",
            entidade = "Veterinario",
            entidadeId = veterinarioId,
        )
        val consultas = consultaRepository.findByVeterinarioIdOrderByDataHoraDesc(veterinarioId)
        val porAnimal = consultas.groupBy { it.animal.id!! }
        val hoje = LocalDate.now()

        return porAnimal.values.map { lista ->
            val animal: Animal = lista.first().animal
            val proxima = vacinacaoRepository.findByAnimalIdOrderByDataAplicacaoDesc(animal.id!!)
                .mapNotNull { it.proximaDose?.let { dose -> Triple(dose, it.tipoVacina, dose.isBefore(hoje)) } }
                .minByOrNull { it.first }
            PacienteClinicoResposta(
                animal = br.ufba.vetcare.api.dto.AnimalResposta.de(animal),
                ultimaConsulta = lista.first().dataHora,
                totalConsultas = lista.size.toLong(),
                proximaVacina = proxima?.first,
                tipoVacina = proxima?.second,
                vacinaAtrasada = proxima?.third ?: false,
            )
        }.sortedBy { it.animal.nome }
    }

    @Transactional(readOnly = true)
    fun meusAtendimentos(veterinarioId: Long): List<AtendimentoResposta> {
        auditoriaService.registrarIndependente(
            acao = "ATENDIMENTOS_CLINICOS_CONSULTADOS",
            entidade = "Veterinario",
            entidadeId = veterinarioId,
        )
        return atendimentoRepository.findByVeterinarioIdOrderByDataHoraDesc(veterinarioId)
            .map(AtendimentoResposta::de)
    }
}
