export type Perfil = 'ADMINISTRADOR' | 'RECEPCIONISTA' | 'VETERINARIO'

export interface Usuario {
  id: number
  nome: string
  email: string
  perfil: Perfil
  perfilDescricao: string
  ativo: boolean
  criadoEm: string
}

export interface Tutor {
  id: number
  nome: string
  cpf: string
  email: string
  telefone: string
  endereco?: string | null
  criadoEm: string
}

export interface Animal {
  id: number
  nome: string
  especie: string
  raca?: string | null
  sexo?: string | null
  dataNascimento?: string | null
  tutorId: number
  tutorNome: string
  ativo: boolean
}

export interface Consulta {
  id: number
  animalId: number
  animalNome: string
  tutorId: number
  tutorNome: string
  veterinarioId: number
  veterinarioNome: string
  dataHora: string
  status: 'AGENDADA' | 'CANCELADA' | 'REALIZADA'
  observacoes?: string | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ErroApi {
  status: number
  erro: string
  mensagem: string
  campos?: Record<string, string>
}
