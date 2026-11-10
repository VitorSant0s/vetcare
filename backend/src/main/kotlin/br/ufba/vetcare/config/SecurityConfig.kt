package br.ufba.vetcare.config

import br.ufba.vetcare.infrastructure.security.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties::class, CorsProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val securityHeadersFilter: br.ufba.vetcare.infrastructure.security.SecurityHeadersFilter,
    private val corsProperties: CorsProperties,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager =
        configuration.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = corsProperties.allowedOrigins.split(",").map { it.trim() }
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = false
        configuration.maxAge = 3600
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/cliente/auth/login",
                        "/actuator/health",
                        "/actuator/info",
                    ).permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                    .requestMatchers("/api/solicitacoes/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers("/api/auditoria/**").hasRole("ADMINISTRADOR")
                    .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.POST, "/api/consultas/*/atendimento")
                    .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                    .requestMatchers(HttpMethod.POST, "/api/animais/*/vacinacoes")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "VETERINARIO")
                    .requestMatchers(HttpMethod.DELETE, "/api/vacinacoes/**")
                    .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                    .requestMatchers("/api/pagamentos/**", "/api/financeiro/**", "/api/relatorios/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers("/api/notificacoes/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers("/api/veterinarios/me/**").hasRole("VETERINARIO")
                    .requestMatchers(HttpMethod.POST, "/api/consultas/*/cancelar")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "VETERINARIO")
                    .requestMatchers("/api/prontuarios/compartilhados/**").hasRole("VETERINARIO")
                    .requestMatchers(HttpMethod.POST, "/api/animais/*/compartilhamento").hasRole("VETERINARIO")
                    .requestMatchers(HttpMethod.DELETE, "/api/animais/*/compartilhamento").hasRole("VETERINARIO")
                    .requestMatchers(HttpMethod.POST, "/api/veterinarios/**").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.PUT, "/api/veterinarios/**").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.DELETE, "/api/veterinarios/**").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.POST, "/api/consultas/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers(HttpMethod.PUT, "/api/consultas/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers(HttpMethod.DELETE, "/api/consultas/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers(HttpMethod.POST, "/api/tutores/**", "/api/animais/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers(HttpMethod.PUT, "/api/tutores/**", "/api/animais/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers(HttpMethod.DELETE, "/api/tutores/**", "/api/animais/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    response.writer.write("""{"status":401,"erro":"Não autenticado","mensagem":"Credenciais ausentes ou token inválido."}""")
                }
                ex.accessDeniedHandler { _, response, _ ->
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    response.writer.write("""{"status":403,"erro":"Acesso negado","mensagem":"Seu perfil não tem permissão para esta operação."}""")
                }
            }
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
