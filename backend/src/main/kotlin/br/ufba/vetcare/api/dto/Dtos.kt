package br.ufba.vetcare.api.dto

import br.ufba.vetcare.domain.Animal
import br.ufba.vetcare.domain.Consulta
import br.ufba.vetcare.domain.Perfil
import br.ufba.vetcare.domain.Tutor
import br.ufba.vetcare.domain.Usuario
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

fun <T : Any> org.springframework.data.domain.Page<T>.toPageResponse(): PageResponse<T> =
    PageResponse(content, number, size, totalElements, totalPages)

data class ErroResposta(
    val status: Int,
    val erro: String,
    val mensagem: String,
    val campos: Map<String, String>? = null,
)

// ---------- Autenticação (REQ02) ----------

data class LoginRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Informe o e-mail")
    @field:jakarta.validation.constraints.Email(message = "E-mail inválido")
    val email: String = "",

    @field:jakarta.validation.constraints.NotBlank(message = "Informe a senha")
    val senha: String = "",
)

data class LoginResponse(
    val token: String,
    val tipo: String = "Bearer",
    val expiraEmSegundos: Long,
    val usuario: UsuarioResposta,
)

// ---------- Usuários (REQ01) ----------

data class UsuarioRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Informe o nome")
    @field:jakarta.validation.constraints.Size(max = 120, message = "Nome muito longo")
    val nome: String = "",

    @field:jakarta.validation.constraints.NotBlank(message = "Informe o e-mail")
    @field:jakarta.validation.constraints.Email(message = "E-mail inválido")
    val email: String = "",

    @field:jakarta.validation.constraints.Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
    val senha: String? = null,

    @field:jakarta.validation.constraints.NotNull(message = "Informe o perfil")
    val perfil: Perfil? = null,

    val ativo: Boolean = true,
)

data class UsuarioResposta(
    val id: Long,
    val nome: String,
    val email: String,
    val perfil: Perfil,
    val perfilDescricao: String,
    val ativo: Boolean,
    val crmv: String? = null,
    val especialidade: String? = null,
    val criadoEm: Instant,
) {
    companion object {
        fun de(usuario: Usuario) = UsuarioResposta(
            id = usuario.id ?: 0,
            nome = usuario.nome,
            email = usuario.email,
            perfil = usuario.perfil,
            perfilDescricao = usuario.perfil.descricao,
            ativo = usuario.ativo,
            crmv = usuario.crmv,
            especialidade = usuario.especialidade,
            criadoEm = usuario.criadoEm,
        )
    }
}

// ---------- Veterinários (REQ06) ----------

data class VeterinarioRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Informe o nome")
    @field:jakarta.validation.constraints.Size(max = 120, message = "Nome muito longo")
    val nome: String = "",

    @field:jakarta.validation.constraints.NotBlank(message = "Informe o e-mail")
    @field:jakarta.validation.constraints.Email(message = "E-mail inválido")
    val email: String = "",

    @field:jakarta.validation.constraints.Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
    val senha: String? = null,

    @field:jakarta.validation.constraints.NotBlank(message = "Informe o CRMV")
    @field:jakarta.validation.constraints.Size(max = 30, message = "CRMV muito longo")
    val crmv: String = "",

    @field:jakarta.validation.constraints.Size(max = 120, message = "Especialidade muito longa")
    val especialidade: String? = null,

    val ativo: Boolean = true,
)

data class VeterinarioResposta(
    val id: Long,
    val nome: String,
    val email: String,
    val perfil: Perfil,
    val perfilDescricao: String,
    val crmv: String?,
    val especialidade: String?,
    val ativo: Boolean,
    val criadoEm: Instant,
) {
    companion object {
        fun de(usuario: Usuario) = VeterinarioResposta(
            id = usuario.id ?: 0,
            nome = usuario.nome,
            email = usuario.email,
            perfil = usuario.perfil,
            perfilDescricao = usuario.perfil.descricao,
            crmv = usuario.crmv,
            especialidade = usuario.especialidade,
            ativo = usuario.ativo,
            criadoEm = usuario.criadoEm,
        )
    }
}

// ---------- Tutores (REQ04) ----------

data class TutorRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Informe o nome")
    @field:jakarta.validation.constraints.Size(max = 120, message = "Nome muito longo")
    val nome: String = "",

    @field:jakarta.validation.constraints.NotBlank(message = "Informe o CPF")
    @field:jakarta.validation.constraints.Size(min = 11, max = 14, message = "CPF inválido")
    val cpf: String = "",

    @field:jakarta.validation.constraints.NotBlank(message = "Informe o e-mail")
    @field:jakarta.validation.constraints.Email(message = "E-mail inválido")
    val email: String = "",

    @field:jakarta.validation.constraints.NotBlank(message = "Informe o telefone")
    @field:jakarta.validation.constraints.Size(max = 20, message = "Telefone muito longo")
    val telefone: String = "",

    @field:jakarta.validation.constraints.Size(max = 200, message = "Endereço muito longo")
    val endereco: String? = null,
)

data class TutorResposta(
    val id: Long,
    val nome: String,
    val cpf: String,
    val email: String,
    val telefone: String,
    val endereco: String?,
    val criadoEm: Instant,
) {
    companion object {
        fun de(tutor: Tutor) = TutorResposta(
            id = tutor.id ?: 0,
            nome = tutor.nome,
            cpf = tutor.cpf,
            email = tutor.email,
            telefone = tutor.telefone,
            endereco = tutor.endereco,
            criadoEm = tutor.criadoEm,
        )
    }
}

// ---------- Animais (REQ05) ----------

data class AnimalRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Informe o nome")
    @field:jakarta.validation.constraints.Size(max = 120, message = "Nome muito longo")
    val nome: String = "",

    @field:jakarta.validation.constraints.NotBlank(message = "Informe a espécie")
    @field:jakarta.validation.constraints.Size(max = 60, message = "Espécie muito longa")
    val especie: String = "",

    @field:jakarta.validation.constraints.Size(max = 60, message = "Raça muito longa")
    val raca: String? = null,

    val sexo: String? = null,

    val dataNascimento: LocalDate? = null,

    @field:jakarta.validation.constraints.NotNull(message = "Selecione o tutor")
    @field:jakarta.validation.constraints.Positive(message = "Tutor inválido")
    val tutorId: Long? = null,
)

data class AnimalResposta(
    val id: Long,
    val nome: String,
    val especie: String,
    val raca: String?,
    val sexo: String?,
    val dataNascimento: LocalDate?,
    val tutorId: Long,
    val tutorNome: String,
    val ativo: Boolean,
) {
    companion object {
        fun de(animal: Animal) = AnimalResposta(
            id = animal.id ?: 0,
            nome = animal.nome,
            especie = animal.especie,
            raca = animal.raca,
            sexo = animal.sexo,
            dataNascimento = animal.dataNascimento,
            tutorId = animal.tutor.id ?: 0,
            tutorNome = animal.tutor.nome,
            ativo = animal.ativo,
        )
    }
}

// ---------- Consultas (REQ07) ----------

data class ConsultaRequest(
    @field:jakarta.validation.constraints.NotNull(message = "Selecione o animal")
    @field:jakarta.validation.constraints.Positive(message = "Animal inválido")
    val animalId: Long? = null,

    @field:jakarta.validation.constraints.NotNull(message = "Selecione o tutor")
    @field:jakarta.validation.constraints.Positive(message = "Tutor inválido")
    val tutorId: Long? = null,

    @field:jakarta.validation.constraints.NotNull(message = "Selecione o veterinário")
    @field:jakarta.validation.constraints.Positive(message = "Veterinário inválido")
    val veterinarioId: Long? = null,

    @field:jakarta.validation.constraints.NotNull(message = "Informe a data e o horário")
    val dataHora: LocalDateTime? = null,

    @field:jakarta.validation.constraints.Size(max = 500, message = "Observações muito longas")
    val observacoes: String? = null,
)

data class ConsultaResposta(
    val id: Long,
    val animalId: Long,
    val animalNome: String,
    val tutorId: Long,
    val tutorNome: String,
    val veterinarioId: Long,
    val veterinarioNome: String,
    val dataHora: LocalDateTime,
    val status: String,
    val observacoes: String?,
    val motivoCancelamento: String? = null,
    val canceladaEm: Instant? = null,
) : java.io.Serializable {
    companion object {
        fun de(consulta: Consulta) = ConsultaResposta(
            id = consulta.id ?: 0,
            animalId = consulta.animal.id ?: 0,
            animalNome = consulta.animal.nome,
            tutorId = consulta.tutor.id ?: 0,
            tutorNome = consulta.tutor.nome,
            veterinarioId = consulta.veterinario.id ?: 0,
            veterinarioNome = consulta.veterinario.nome,
            dataHora = consulta.dataHora,
            status = consulta.status.name,
            observacoes = consulta.observacoes,
            motivoCancelamento = consulta.motivoCancelamento,
            canceladaEm = consulta.canceladaEm,
        )
    }
}

data class CancelamentoRequest(
    @field:jakarta.validation.constraints.Size(max = 300, message = "Motivo muito longo")
    val motivo: String? = null,
)

// ---------- Atendimento e prescrição (REQ10 e REQ13) ----------

data class PrescricaoRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Informe o medicamento")
    @field:jakarta.validation.constraints.Size(max = 160, message = "Medicamento muito longo")
    val medicamento: String = "",

    @field:jakarta.validation.constraints.Size(max = 120, message = "Dosagem muito longa")
    val dosagem: String? = null,

    @field:jakarta.validation.constraints.Size(max = 120, message = "Frequência muito longa")
    val frequencia: String? = null,

    @field:jakarta.validation.constraints.Size(max = 120, message = "Duração muito longa")
    val duracao: String? = null,

    @field:jakarta.validation.constraints.Size(max = 500, message = "Orientações muito longas")
    val orientacoes: String? = null,
)

data class PrescricaoResposta(
    val id: Long,
    val medicamento: String,
    val dosagem: String?,
    val frequencia: String?,
    val duracao: String?,
    val orientacoes: String?,
) {
    companion object {
        fun de(prescricao: br.ufba.vetcare.domain.Prescricao) = PrescricaoResposta(
            id = prescricao.id ?: 0,
            medicamento = prescricao.medicamento,
            dosagem = prescricao.dosagem,
            frequencia = prescricao.frequencia,
            duracao = prescricao.duracao,
            orientacoes = prescricao.orientacoes,
        )
    }
}

data class AtendimentoRequest(
    @field:jakarta.validation.constraints.Size(max = 2000, message = "Anamnese muito longa")
    val anamnese: String? = null,

    @field:jakarta.validation.constraints.NotBlank(message = "Informe o diagnóstico")
    @field:jakarta.validation.constraints.Size(max = 2000, message = "Diagnóstico muito longo")
    val diagnostico: String = "",

    @field:jakarta.validation.constraints.Size(max = 2000, message = "Procedimentos muito longos")
    val procedimentos: String? = null,

    @field:jakarta.validation.constraints.Size(max = 2000, message = "Observações muito longas")
    val observacoes: String? = null,

    @field:jakarta.validation.constraints.Size(max = 20, message = "Máximo de 20 prescrições por atendimento")
    val prescricoes: List<PrescricaoRequest> = emptyList(),
)

data class AtendimentoResposta(
    val id: Long,
    val consultaId: Long,
    val animalId: Long,
    val animalNome: String,
    val veterinarioId: Long,
    val veterinarioNome: String,
    val dataHora: LocalDateTime,
    val anamnese: String?,
    val diagnostico: String,
    val procedimentos: String?,
    val observacoes: String?,
    val prescricoes: List<PrescricaoResposta>,
    val criadoEm: Instant,
) {
    companion object {
        fun de(atendimento: br.ufba.vetcare.domain.Atendimento) = AtendimentoResposta(
            id = atendimento.id ?: 0,
            consultaId = atendimento.consulta.id ?: 0,
            animalId = atendimento.animal.id ?: 0,
            animalNome = atendimento.animal.nome,
            veterinarioId = atendimento.veterinario.id ?: 0,
            veterinarioNome = atendimento.veterinario.nome,
            dataHora = atendimento.dataHora,
            anamnese = atendimento.anamnese,
            diagnostico = atendimento.diagnostico,
            procedimentos = atendimento.procedimentos,
            observacoes = atendimento.observacoes,
            prescricoes = atendimento.prescricoes.map(PrescricaoResposta::de),
            criadoEm = atendimento.criadoEm,
        )
    }
}

// ---------- Vacinação (REQ12) ----------

data class VacinacaoRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Informe o tipo de vacina")
    @field:jakarta.validation.constraints.Size(max = 120, message = "Tipo de vacina muito longo")
    val tipoVacina: String = "",

    @field:jakarta.validation.constraints.Size(max = 60, message = "Lote muito longo")
    val lote: String? = null,

    @field:jakarta.validation.constraints.NotNull(message = "Informe a data de aplicação")
    val dataAplicacao: LocalDate? = null,

    val proximaDose: LocalDate? = null,

    @field:jakarta.validation.constraints.Size(max = 500, message = "Observações muito longas")
    val observacoes: String? = null,
)

data class VacinacaoResposta(
    val id: Long,
    val animalId: Long,
    val animalNome: String,
    val veterinarioId: Long?,
    val veterinarioNome: String?,
    val tipoVacina: String,
    val lote: String?,
    val dataAplicacao: LocalDate,
    val proximaDose: LocalDate?,
    val observacoes: String?,
    val criadoEm: Instant,
) {
    companion object {
        fun de(vacinacao: br.ufba.vetcare.domain.Vacinacao) = VacinacaoResposta(
            id = vacinacao.id ?: 0,
            animalId = vacinacao.animal.id ?: 0,
            animalNome = vacinacao.animal.nome,
            veterinarioId = vacinacao.veterinario?.id,
            veterinarioNome = vacinacao.veterinario?.nome,
            tipoVacina = vacinacao.tipoVacina,
            lote = vacinacao.lote,
            dataAplicacao = vacinacao.dataAplicacao,
            proximaDose = vacinacao.proximaDose,
            observacoes = vacinacao.observacoes,
            criadoEm = vacinacao.criadoEm,
        )
    }
}

// ---------- Prontuário (REQ11) ----------

data class ProntuarioResposta(
    val animal: AnimalResposta,
    val tutor: TutorResposta,
    val atendimentos: List<AtendimentoResposta>,
    val vacinacoes: List<VacinacaoResposta>,
    val consultas: List<ConsultaResposta>,
)
