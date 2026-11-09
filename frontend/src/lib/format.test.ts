import { describe, expect, it } from 'vitest'
import {
  daquiADiasIso,
  formatarCpf,
  formatarData,
  formatarTelefone,
  idade,
  paraInputDateTime,
  somarDiasIso,
  statusProximaDose,
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

  it('soma dias a uma data ISO', () => {
    expect(somarDiasIso('2026-11-09', 7)).toBe('2026-11-16')
    expect(somarDiasIso('2026-12-30', 3)).toBe('2027-01-02')
  })

  it('classifica a próxima dose da vacina', () => {
    const emDezDias = new Date()
    emDezDias.setDate(emDezDias.getDate() + 10)
    const dez = paraInputDateTime(emDezDias).slice(0, 10)
    expect(statusProximaDose(dez)).toEqual({ texto: 'Em 10 dias', tom: 'ambar' })
    expect(statusProximaDose(null).tom).toBe('neutro')

    const atrasada = new Date()
    atrasada.setDate(atrasada.getDate() - 5)
    const cincoAtras = paraInputDateTime(atrasada).slice(0, 10)
    expect(statusProximaDose(cincoAtras).texto).toBe('Atrasada há 5d')
  })

  it('gera data futura no formato ISO de input', () => {
    const hoje = paraInputDateTime(new Date()).slice(0, 10)
    expect(daquiADiasIso(0)).toBe(hoje)
    expect(daquiADiasIso(1) > hoje).toBe(true)
  })
})
