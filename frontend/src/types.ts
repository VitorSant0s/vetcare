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
  ativo?: boolean
  portalConfigurado?: boolean
  primeiroAcesso?: boolean
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

export type FormaPagamento = 'DINHEIRO' | 'PIX' | 'CARTAO_CREDITO' | 'CARTAO_DEBITO' | 'CONVENIO'
export type StatusPagamento = 'PAGO' | 'PENDENTE' | 'CANCELADO'

export interface Pagamento {
  id: number
  tutorId: number
  tutorNome: string
  consultaId?: number | null
  consultaDescricao?: string | null
  valor: number
  formaPagamento: FormaPagamento
  formaPagamentoDescricao: string
  status: StatusPagamento
  dataPagamento: string
  observacoes?: string | null
  criadoEm: string
}

export interface FormaTotal {
  forma: FormaPagamento
  descricao: string
  total: number
  quantidade: number
}

export interface DashboardFinanceiro {
  inicio: string
  fim: string
  totalRecebido: number
  quantidadePagamentos: number
  ticketMedio: number
  atendimentos: number
  porForma: FormaTotal[]
}

export interface RelatorioResumo {
  inicio: string
  fim: string
  consultasTotal: number
  consultasAgendadas: number
  consultasRealizadas: number
  consultasCanceladas: number
  atendimentos: number
  animaisCadastrados: number
  animaisNovos: number
  vacinasAplicadas: number
  totalRecebido: number
  porForma: FormaTotal[]
}

export interface Notificacao {
  id: number
  tutorId: number
  tutorNome: string
  tipo: 'CONSULTA' | 'VACINA'
  canal: string
  destinatario: string
  mensagem: string
  status: 'PENDENTE' | 'ENVIADA' | 'FALHA'
  erro?: string | null
  criadoEm: string
  enviadoEm?: string | null
}

export interface SolicitacaoAgendamento {
  id: number
  tutorId: number
  tutorNome: string
  animalId: number
  animalNome: string
  dataPreferida: string
  motivo?: string | null
  status: 'PENDENTE' | 'APROVADA' | 'RECUSADA' | 'CANCELADA'
  observacaoResposta?: string | null
  consultaId?: number | null
  criadoEm: string
  analisadoEm?: string | null
}
