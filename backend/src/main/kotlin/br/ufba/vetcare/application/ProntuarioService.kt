package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AnimalResposta
import br.ufba.vetcare.api.dto.AtendimentoResposta
import br.ufba.vetcare.api.dto.ConsultaResposta
import br.ufba.vetcare.api.dto.ProntuarioResposta
import br.ufba.vetcare.api.dto.TutorResposta
import br.ufba.vetcare.api.dto.VacinacaoResposta
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.domain.repository.AnimalRepository
import br.ufba.vetcare.domain.repository.AtendimentoRepository
import br.ufba.vetcare.domain.repository.ConsultaRepository
import br.ufba.vetcare.domain.repository.VacinacaoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * REQ11 - Prontuário: consolida atendimentos, prescrições, vacinações e consultas do animal.
 */
@Service
class ProntuarioService(
    private val animalRepository: AnimalRepository,
    private val atendimentoRepository: AtendimentoRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val consultaRepository: ConsultaRepository,
    private val auditoriaService: AuditoriaService,
) {

    @Transactional
    fun montar(animalId: Long): ProntuarioResposta {
        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Animal não encontrado.") }

        val resposta = ProntuarioResposta(
            animal = AnimalResposta.de(animal),
            tutor = TutorResposta.de(animal.tutor),
            atendimentos = atendimentoRepository.findByAnimalIdOrderByDataHoraDesc(animalId)
                .map(AtendimentoResposta::de),
            vacinacoes = vacinacaoRepository.findByAnimalIdOrderByDataAplicacaoDesc(animalId)
                .map(VacinacaoResposta::de),
            consultas = consultaRepository.findByAnimalIdOrderByDataHoraDesc(animalId)
                .map(ConsultaResposta::de),
        )

        auditoriaService.registrar(
            acao = "PRONTUARIO_ACESSADO",
            entidade = "Animal",
            entidadeId = animalId,
            detalhes = animal.nome,
        )
        return resposta
    }
}
