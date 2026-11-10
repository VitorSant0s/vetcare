package br.ufba.vetcare.api

import br.ufba.vetcare.api.dto.ErroResposta
import br.ufba.vetcare.application.exception.AutenticacaoException
import br.ufba.vetcare.application.exception.ConflitoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.application.exception.TooManyRequestsException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(RecursoNaoEncontradoException::class)
    fun naoEncontrado(ex: RecursoNaoEncontradoException) =
        resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.message)

    @ExceptionHandler(RegraNegocioException::class)
    fun regraNegocio(ex: RegraNegocioException) =
        resposta(HttpStatus.BAD_REQUEST, "Regra de negócio", ex.message)

    @ExceptionHandler(ConflitoException::class)
    fun conflito(ex: ConflitoException) =
        resposta(HttpStatus.CONFLICT, "Conflito", ex.message)

    @ExceptionHandler(AutenticacaoException::class)
    fun autenticacao(ex: AutenticacaoException) =
        resposta(HttpStatus.UNAUTHORIZED, "Não autenticado", ex.message)

    @ExceptionHandler(TooManyRequestsException::class)
    fun muitasTentativas(ex: TooManyRequestsException) =
        resposta(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas", ex.message)

    @ExceptionHandler(AccessDeniedException::class)
    fun acessoNegado(ex: AccessDeniedException) =
        resposta(HttpStatus.FORBIDDEN, "Acesso negado", "Seu perfil não tem permissão para esta operação.")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacao(ex: MethodArgumentNotValidException): ResponseEntity<ErroResposta> {
        val campos = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Valor inválido") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErroResposta(
                status = 400,
                erro = "Dados inválidos",
                mensagem = "Verifique os campos informados.",
                campos = campos,
            ),
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    fun requisicaoInvalida(ex: Exception) =
        resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", "Formato de dados inválido.")

    @ExceptionHandler(NoResourceFoundException::class)
    fun rotaNaoEncontrada(ex: NoResourceFoundException) =
        resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", "A rota informada não existe.")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun metodoNaoSuportado(ex: HttpRequestMethodNotSupportedException) =
        resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido", "Método HTTP não suportado para esta rota.")

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun integridade(ex: DataIntegrityViolationException): ResponseEntity<ErroResposta> {
        log.warn("Violação de integridade: {}", ex.mostSpecificCause.message)
        return resposta(HttpStatus.CONFLICT, "Conflito", "A operação viola uma regra de integridade dos dados.")
    }

    @ExceptionHandler(Exception::class)
    fun inesperado(ex: Exception): ResponseEntity<ErroResposta> {
        log.error("Erro inesperado", ex)
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Não foi possível concluir a operação.")
    }

    private fun resposta(status: HttpStatus, erro: String, mensagem: String?) =
        ResponseEntity.status(status).body(
            ErroResposta(status = status.value(), erro = erro, mensagem = mensagem ?: erro),
        )
}
