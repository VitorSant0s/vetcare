package br.ufba.vetcare.domain

/**
 * Perfis de acesso previstos no Documento de Requisitos (atributo de qualidade Segurança).
 */
enum class Perfil {
    ADMINISTRADOR,
    RECEPCIONISTA,
    VETERINARIO;

    val descricao: String
        get() = when (this) {
            ADMINISTRADOR -> "Administrador"
            RECEPCIONISTA -> "Recepcionista"
            VETERINARIO -> "Veterinário"
        }
}
