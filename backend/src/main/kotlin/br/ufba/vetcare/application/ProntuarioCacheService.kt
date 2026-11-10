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
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Parte pesada do prontuário, cacheada no Redis por animal.
 * A autorização e a auditoria ficam no ProntuarioService, fora do cache.
 */
@Service
class ProntuarioCacheService(
    private val animalRepository: AnimalRepository,
    private val atendimentoRepository: AtendimentoRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val consultaRepository: ConsultaRepository,
) {

    @Cacheable(cacheNames = ["prontuario"], key = "#animalId")
    @Transactional(readOnly = true)
    fun carregar(animalId: Long): ProntuarioResposta {
        val animal = animalRepository.findById(animalId)
            .orElseThrow { RecursoNaoEncontradoException("Animal não encontrado.") }

        return ProntuarioResposta(
            animal = AnimalResposta.de(animal),
            tutor = TutorResposta.de(animal.tutor),
            atendimentos = atendimentoRepository.findByAnimalIdOrderByDataHoraDesc(animalId)
                .map(AtendimentoResposta::de),
            vacinacoes = vacinacaoRepository.findByAnimalIdOrderByDataAplicacaoDesc(animalId)
                .map(VacinacaoResposta::de),
            consultas = consultaRepository.findByAnimalIdOrderByDataHoraDesc(animalId)
                .map(ConsultaResposta::de),
        )
    }
}
