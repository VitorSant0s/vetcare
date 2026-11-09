export type Perfil = 'ADMINISTRADOR' | 'RECEPCIONISTA' | 'VETERINARIO'

export interface Usuario {
  id: number
  nome: string
  email: string
  perfil: Perfil
  perfilDescricao: string
  ativo: boolean
  crmv?: string | null
  especialidade?: string | null
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

export interface Veterinario {
  id: number
  nome: string
  email: string
  perfil: Perfil
  perfilDescricao: string
  crmv?: string | null
  especialidade?: string | null
  ativo: boolean
  criadoEm: string
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
  motivoCancelamento?: string | null
  canceladaEm?: string | null
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

export interface Prescricao {
  id: number
  medicamento: string
  dosagem?: string | null
  frequencia?: string | null
  duracao?: string | null
  orientacoes?: string | null
}

export interface Atendimento {
  id: number
  consultaId: number
  animalId: number
  animalNome: string
  veterinarioId: number
  veterinarioNome: string
  dataHora: string
  anamnese?: string | null
  diagnostico: string
  procedimentos?: string | null
  observacoes?: string | null
  prescricoes: Prescricao[]
  criadoEm: string
}

export interface Vacinacao {
  id: number
  animalId: number
  animalNome: string
  veterinarioId?: number | null
  veterinarioNome?: string | null
  tipoVacina: string
  lote?: string | null
  dataAplicacao: string
  proximaDose?: string | null
  observacoes?: string | null
  criadoEm: string
}

export interface Prontuario {
  animal: Animal
  tutor: Tutor
  atendimentos: Atendimento[]
  vacinacoes: Vacinacao[]
  consultas: Consulta[]
}
