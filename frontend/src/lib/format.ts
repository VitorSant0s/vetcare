export function formatarData(iso: string | null | undefined): string {
  if (!iso) return '—'
  const data = iso.length === 10 ? new Date(`${iso}T00:00:00`) : new Date(iso)
  if (Number.isNaN(data.getTime())) return '—'
  return new Intl.DateTimeFormat('pt-BR').format(data)
}

export function formatarDataHora(iso: string | null | undefined): string {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return '—'
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(data)
}

export function formatarHora(iso: string): string {
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return '—'
  return new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit' }).format(data)
}

export function formatarCpf(cpf: string): string {
  const digitos = cpf.replace(/\D/g, '').slice(0, 11)
  return digitos
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
}

export function formatarTelefone(telefone: string): string {
  const digitos = telefone.replace(/\D/g, '').slice(0, 11)
  if (digitos.length <= 10) {
    return digitos.replace(/(\d{2})(\d)/, '($1) $2').replace(/(\d{4})(\d)/, '$1-$2')
  }
  return digitos.replace(/(\d{2})(\d)/, '($1) $2').replace(/(\d{5})(\d)/, '$1-$2')
}

export function paraInputDateTime(data: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${data.getFullYear()}-${pad(data.getMonth() + 1)}-${pad(data.getDate())}T${pad(data.getHours())}:${pad(data.getMinutes())}`
}

export function hojeIso(): string {
  return paraInputDateTime(new Date()).slice(0, 10)
}

export function daquiADiasIso(dias: number): string {
  const data = new Date()
  data.setDate(data.getDate() + dias)
  return paraInputDateTime(data).slice(0, 10)
}

export function idade(iso: string | null | undefined): string {
  if (!iso) return '—'
  const nascimento = new Date(`${iso}T00:00:00`)
  if (Number.isNaN(nascimento.getTime())) return '—'
  const hoje = new Date()
  let anos = hoje.getFullYear() - nascimento.getFullYear()
  const aindaNaoFezAniversario =
    hoje.getMonth() < nascimento.getMonth() ||
    (hoje.getMonth() === nascimento.getMonth() && hoje.getDate() < nascimento.getDate())
  if (aindaNaoFezAniversario) anos--
  if (anos < 1) {
    const meses = Math.max(
      0,
      (hoje.getFullYear() - nascimento.getFullYear()) * 12 +
        (hoje.getMonth() - nascimento.getMonth()),
    )
    return `${meses} ${meses === 1 ? 'mês' : 'meses'}`
  }
  return `${anos} ${anos === 1 ? 'ano' : 'anos'}`
}

export function somarDiasIso(iso: string, dias: number): string {
  const data = new Date(`${iso}T12:00:00`)
  data.setDate(data.getDate() + dias)
  return paraInputDateTime(data).slice(0, 10)
}

export type TomDose = 'verde' | 'ambar' | 'vermelho' | 'neutro'

export function statusProximaDose(iso?: string | null): { texto: string; tom: TomDose } {
  if (!iso) return { texto: 'Sem previsão', tom: 'neutro' }
  const hoje = new Date(`${hojeIso()}T00:00:00`)
  const dose = new Date(`${iso}T00:00:00`)
  const dias = Math.round((dose.getTime() - hoje.getTime()) / 86400000)
  if (dias < 0) return { texto: `Atrasada há ${Math.abs(dias)}d`, tom: 'vermelho' }
  if (dias === 0) return { texto: 'Hoje', tom: 'ambar' }
  if (dias <= 30) return { texto: `Em ${dias} dias`, tom: 'ambar' }
  return { texto: formatarData(iso), tom: 'verde' }
}
