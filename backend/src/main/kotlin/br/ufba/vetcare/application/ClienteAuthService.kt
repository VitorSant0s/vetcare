package br.ufba.vetcare.application

import br.ufba.vetcare.api.dto.AlterarSenhaRequest
import br.ufba.vetcare.api.dto.ClienteLoginRequest
import br.ufba.vetcare.api.dto.ClienteLoginResponse
import br.ufba.vetcare.api.dto.TutorResposta
import br.ufba.vetcare.application.exception.AutenticacaoException
import br.ufba.vetcare.application.exception.RecursoNaoEncontradoException
import br.ufba.vetcare.application.exception.RegraNegocioException
import br.ufba.vetcare.domain.Cpf
import br.ufba.vetcare.domain.Tutor
import br.ufba.vetcare.domain.repository.TutorRepository
import br.ufba.vetcare.infrastructure.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Autenticação do cliente (tutor) no portal: CPF + senha definida pela recepção.
 */
@Service
class ClienteAuthService(
    private val tutorRepository: TutorRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val auditoriaService: AuditoriaService,
    private val rateLimiter: LoginRateLimiter,
) {

    @Transactional
    fun login(request: ClienteLoginRequest): ClienteLoginResponse {
        rateLimiter.verificar(Cpf.normalizar(request.cpf))
        val cpf = Cpf.normalizar(request.cpf)
        val tutor = tutorRepository.findByCpf(cpf).orElse(null)
        if (tutor == null || tutor.senha == null || !passwordEncoder.matches(request.senha, tutor.senha)) {
            val bloqueou = rateLimiter.registrarFalha(cpf)
            auditoriaService.registrar(
                acao = "LOGIN_CLIENTE_FALHA",
                entidade = "Tutor",
                detalhes = "CPF informado: $cpf",
                usuarioEmail = cpf,
            )
            if (bloqueou) {
                auditoriaService.registrar(
                    acao = "LOGIN_CLIENTE_BLOQUEADO",
                    entidade = "Tutor",
                    detalhes = "Excesso de tentativas para o CPF $cpf",
                    usuarioEmail = cpf,
                )
            }
            throw AutenticacaoException("CPF ou senha inválidos.")
        }
        if (!tutor.ativo) {
            throw AutenticacaoException("Cadastro inativo. Procure a clínica.")
        }
        if (tutor.anonimizado) {
            throw AutenticacaoException("Cadastro anonimizado. Procure a clínica.")
        }
        rateLimiter.limpar(cpf)
        auditoriaService.registrar(
            acao = "LOGIN_CLIENTE_SUCESSO",
            entidade = "Tutor",
            entidadeId = tutor.id,
            usuarioEmail = tutor.email,
        )
        return ClienteLoginResponse(
            token = jwtService.gerarTokenCliente(tutor),
            expiraEmSegundos = jwtService.expiracaoEmSegundos(),
            tutor = TutorResposta.de(tutor),
            primeiroAcesso = tutor.primeiroAcesso,
        )
    }

    @Transactional(readOnly = true)
    fun me(tutorId: Long): TutorResposta =
        TutorResposta.de(obter(tutorId))

    @Transactional
    fun alterarSenha(tutorId: Long, request: AlterarSenhaRequest) {
        val tutor = obter(tutorId)
        if (tutor.senha == null || !passwordEncoder.matches(request.senhaAtual, tutor.senha)) {
            throw RegraNegocioException("Senha atual incorreta.")
        }
        if (request.senhaAtual == request.novaSenha) {
            throw RegraNegocioException("A nova senha deve ser diferente da atual.")
        }
        tutor.senha = passwordEncoder.encode(request.novaSenha)!!
        tutor.primeiroAcesso = false
        tutor.tokenVersion += 1
        tutorRepository.save(tutor)
        auditoriaService.registrar(
            acao = "SENHA_CLIENTE_ALTERADA",
            entidade = "Tutor",
            entidadeId = tutorId,
            usuarioEmail = tutor.email,
        )
    }

    @Transactional
    fun logout(tutorId: Long) {
        val tutor = tutorRepository.findById(tutorId).orElse(null) ?: return
        tutor.tokenVersion += 1
        tutorRepository.save(tutor)
        auditoriaService.registrar(
            acao = "LOGOUT_CLIENTE",
            entidade = "Tutor",
            entidadeId = tutorId,
            usuarioEmail = tutor.email,
        )
    }

    @Transactional
    fun registrarConsentimento(tutorId: Long, versao: String): TutorResposta {
        val tutor = obter(tutorId)
        tutor.consentimentoEm = Instant.now()
        tutor.consentimentoVersao = versao.trim()
        tutorRepository.save(tutor)
        auditoriaService.registrar(
            acao = "CONSENTIMENTO_LGPD_REGISTRADO",
            entidade = "Tutor",
            entidadeId = tutorId,
            detalhes = "Versão ${tutor.consentimentoVersao}",
            usuarioEmail = tutor.email,
        )
        return TutorResposta.de(tutor)
    }

    private fun obter(tutorId: Long): Tutor =
        tutorRepository.findById(tutorId)
            .orElseThrow { RecursoNaoEncontradoException("Cliente não encontrado.") }
}
