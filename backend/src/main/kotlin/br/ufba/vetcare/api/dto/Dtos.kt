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
