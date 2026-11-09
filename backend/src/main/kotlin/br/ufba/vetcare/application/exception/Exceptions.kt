package br.ufba.vetcare.application.exception

class RecursoNaoEncontradoException(mensagem: String) : RuntimeException(mensagem)

class RegraNegocioException(mensagem: String) : RuntimeException(mensagem)

class ConflitoException(mensagem: String) : RuntimeException(mensagem)

class AutenticacaoException(mensagem: String) : RuntimeException(mensagem)
