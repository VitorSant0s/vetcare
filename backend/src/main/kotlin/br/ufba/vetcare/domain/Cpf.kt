package br.ufba.vetcare.domain

/**
 * Validação de CPF (formato e dígitos verificadores). O armazenamento usa apenas dígitos.
 */
object Cpf {

    fun normalizar(valor: String): String = valor.filter { it.isDigit() }

    fun valido(valor: String): Boolean {
        val digitos = normalizar(valor)
        if (digitos.length != 11 || digitos.all { it == digitos[0] }) return false
        val numeros = digitos.map { it.digitToInt() }

        fun digitoVerificador(base: List<Int>): Int {
            val pesoInicial = base.size + 1
            val soma = base.mapIndexed { indice, numero -> numero * (pesoInicial - indice) }.sum()
            val resto = soma % 11
            return if (resto < 2) 0 else 11 - resto
        }

        val primeiro = digitoVerificador(numeros.take(9))
        val segundo = digitoVerificador(numeros.take(10))
        return numeros[9] == primeiro && numeros[10] == segundo
    }
}
