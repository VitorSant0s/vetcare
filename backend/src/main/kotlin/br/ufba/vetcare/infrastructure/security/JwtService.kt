package br.ufba.vetcare.infrastructure.security

import br.ufba.vetcare.config.JwtProperties
import br.ufba.vetcare.domain.Usuario
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(private val properties: JwtProperties) {

    private val key: SecretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))

    fun gerarToken(usuario: Usuario): String {
        val agora = Instant.now()
        val expiraEm = agora.plusSeconds(properties.expirationMinutes * 60)
        return Jwts.builder()
            .subject(usuario.email)
            .claim("id", usuario.id)
            .claim("nome", usuario.nome)
            .claim("perfil", usuario.perfil.name)
            .issuedAt(Date.from(agora))
            .expiration(Date.from(expiraEm))
            .signWith(key)
            .compact()
    }

    fun extrairClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    fun tokenValido(token: String): Boolean =
        runCatching { extrairClaims(token) }.isSuccess

    fun expiracaoEmSegundos(): Long = properties.expirationMinutes * 60
}
