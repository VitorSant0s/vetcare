import { describe, expect, it } from 'vitest'
import {
  daquiADiasIso,
  formatarCpf,
  formatarData,
  formatarTelefone,
  idade,
  paraInputDateTime,
} from '../lib/format'

describe('formatadores', () => {
  it('formata CPF a partir de dígitos', () => {
    expect(formatarCpf('12345678901')).toBe('123.456.789-01')
  })

  it('formata telefone celular com dígitos ignorados', () => {
    expect(formatarTelefone('(71) 98888-1111')).toBe('(71) 98888-1111')
  })

  it('formata telefone fixo', () => {
    expect(formatarTelefone('7133334444')).toBe('(71) 3333-4444')
  })

  it('formata data ISO para o padrão brasileiro', () => {
    expect(formatarData('2024-03-14')).toBe('14/03/2024')
  })

  it('retorna travessão para data ausente', () => {
    expect(formatarData(null)).toBe('—')
  })

  it('calcula idade em anos completos', () => {
    const doisAnosAtras = new Date()
    doisAnosAtras.setFullYear(doisAnosAtras.getFullYear() - 2)
    doisAnosAtras.setDate(doisAnosAtras.getDate() - 1)
    expect(idade(paraInputDateTime(doisAnosAtras).slice(0, 10))).toBe('2 anos')
  })

  it('gera data futura no formato ISO de input', () => {
    const hoje = paraInputDateTime(new Date()).slice(0, 10)
    expect(daquiADiasIso(0)).toBe(hoje)
    expect(daquiADiasIso(1) > hoje).toBe(true)
  })
})
