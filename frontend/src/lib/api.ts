import axios from 'axios'
import type {
  Animal,
  Consulta,
  ErroApi,
  PageResponse,
  Tutor,
  Usuario,
  Veterinario,
  Atendimento,
  Prontuario,
  Vacinacao,
  Pagamento,
  DashboardFinanceiro,
  RelatorioResumo,
  Notificacao,
  FormaPagamento,
  StatusPagamento,
  SolicitacaoAgendamento,
  PacienteClinico,
  CompartilhamentoProntuario,
  Auditoria,
} from '../types'

const TOKEN_KEY = 'vetcare.token'

export const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url: string = error.config?.url ?? ''
    if (status === 401 && !url.includes('/auth/login')) {
      localStorage.removeItem(TOKEN_KEY)
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  },
)

export function mensagemDeErro(error: unknown): string {
  if (axios.isAxiosError<ErroApi>(error)) {
    return error.response?.data?.mensagem ?? 'Não foi possível concluir a operação.'
  }
  return 'Não foi possível concluir a operação.'
}

export function camposDeErro(error: unknown): Record<string, string> {
  if (axios.isAxiosError<ErroApi>(error)) {
    return error.response?.data?.campos ?? {}
  }
  return {}
}

export const authApi = {
  login: (email: string, senha: string) =>
    api.post<{ token: string; tipo: string; expiraEmSegundos: number; usuario: Usuario }>(
      '/auth/login',
      { email, senha },
    ),
  me: () => api.get<Usuario>('/auth/me'),
}

export interface UsuarioPayload {
  nome: string
  email: string
  senha?: string
  perfil: string
  ativo: boolean
}

export const usuariosApi = {
  listar: (termo?: string, page = 0, size = 20) =>
    api.get<PageResponse<Usuario>>('/usuarios', { params: { termo, page, size } }),
  criar: (payload: UsuarioPayload) => api.post<Usuario>('/usuarios', payload),
  atualizar: (id: number, payload: UsuarioPayload) =>
    api.put<Usuario>(`/usuarios/${id}`, payload),
  excluir: (id: number) => api.delete(`/usuarios/${id}`),
}

export interface TutorPayload {
  nome: string
  cpf: string
  email: string
  telefone: string
  endereco?: string | null
  senha?: string
  ativo: boolean
}

export const tutoresApi = {
  listar: (termo?: string, page = 0, size = 20) =>
    api.get<PageResponse<Tutor>>('/tutores', { params: { termo, page, size } }),
  criar: (payload: TutorPayload) => api.post<Tutor>('/tutores', payload),
  atualizar: (id: number, payload: TutorPayload) => api.put<Tutor>(`/tutores/${id}`, payload),
  excluir: (id: number) => api.delete(`/tutores/${id}`),
}

export interface AnimalPayload {
  nome: string
  especie: string
  raca?: string | null
  sexo?: string | null
  dataNascimento?: string | null
  tutorId: number
}

export const animaisApi = {
  listar: (termo?: string, page = 0, size = 20) =>
    api.get<PageResponse<Animal>>('/animais', { params: { termo, page, size } }),
  porTutor: (tutorId: number) => api.get<Animal[]>(`/animais/por-tutor/${tutorId}`),
  criar: (payload: AnimalPayload) => api.post<Animal>('/animais', payload),
  atualizar: (id: number, payload: AnimalPayload) => api.put<Animal>(`/animais/${id}`, payload),
  excluir: (id: number) => api.delete(`/animais/${id}`),
}

export interface ConsultaPayload {
  animalId: number
  tutorId: number
  veterinarioId: number
  dataHora: string
  observacoes?: string | null
}

export const consultasApi = {
  listar: (inicio: string, fim: string, veterinarioId?: number) =>
    api.get<Consulta[]>('/consultas', { params: { inicio, fim, veterinarioId } }),
  criar: (payload: ConsultaPayload) => api.post<Consulta>('/consultas', payload),
  cancelar: (id: number, motivo?: string) =>
    api.post<Consulta>(`/consultas/${id}/cancelar`, { motivo }),
}

export interface VeterinarioPayload {
  nome: string
  email: string
  senha?: string
  crmv: string
  especialidade?: string | null
  ativo: boolean
}

export const veterinariosApi = {
  listar: (incluirInativos = false) =>
    api.get<Veterinario[]>('/veterinarios', { params: { incluirInativos } }),
  meusPacientes: () => api.get<PacienteClinico[]>('/veterinarios/me/pacientes'),
  meusAtendimentos: () => api.get<Atendimento[]>('/veterinarios/me/atendimentos'),
  criar: (payload: VeterinarioPayload) => api.post<Veterinario>('/veterinarios', payload),
  atualizar: (id: number, payload: VeterinarioPayload) =>
    api.put<Veterinario>(`/veterinarios/${id}`, payload),
  excluir: (id: number) => api.delete(`/veterinarios/${id}`),
}


export interface PrescricaoPayload {
  medicamento: string
  dosagem?: string | null
  frequencia?: string | null
  duracao?: string | null
  orientacoes?: string | null
}

export interface AtendimentoPayload {
  anamnese?: string | null
  diagnostico: string
  procedimentos?: string | null
  observacoes?: string | null
  prescricoes: PrescricaoPayload[]
}

export const atendimentosApi = {
  registrar: (consultaId: number, payload: AtendimentoPayload) =>
    api.post<Atendimento>(`/consultas/${consultaId}/atendimento`, payload),
}

export const prontuarioApi = {
  buscar: (animalId: number) => api.get<Prontuario>(`/animais/${animalId}/prontuario`),
  compartilhar: (animalId: number) =>
    api.post<CompartilhamentoProntuario>(`/animais/${animalId}/compartilhamento`),
  revogar: (animalId: number) => api.delete(`/animais/${animalId}/compartilhamento`),
  buscarPorCodigo: (codigo: string) =>
    api.get<Prontuario>(`/prontuarios/compartilhados/${encodeURIComponent(codigo)}`),
}

export interface VacinacaoPayload {
  tipoVacina: string
  lote?: string | null
  dataAplicacao: string
  proximaDose?: string | null
  observacoes?: string | null
}

export const vacinacoesApi = {
  listar: (animalId: number) => api.get<Vacinacao[]>(`/animais/${animalId}/vacinacoes`),
  criar: (animalId: number, payload: VacinacaoPayload) =>
    api.post<Vacinacao>(`/animais/${animalId}/vacinacoes`, payload),
  excluir: (id: number) => api.delete(`/vacinacoes/${id}`),
}


export interface PagamentoPayload {
  tutorId: number
  consultaId?: number | null
  valor: number
  formaPagamento: FormaPagamento
  dataPagamento: string
  status?: StatusPagamento
  observacoes?: string | null
}

export const pagamentosApi = {
  listar: (inicio: string, fim: string) =>
    api.get<Pagamento[]>('/pagamentos', { params: { inicio, fim } }),
  criar: (payload: PagamentoPayload) => api.post<Pagamento>('/pagamentos', payload),
  dashboard: (inicio: string, fim: string) =>
    api.get<DashboardFinanceiro>('/financeiro/dashboard', { params: { inicio, fim } }),
}

export const relatoriosApi = {
  resumo: (inicio: string, fim: string) =>
    api.get<RelatorioResumo>('/relatorios/resumo', { params: { inicio, fim } }),
}

export const notificacoesApi = {
  listar: () => api.get<Notificacao[]>('/notificacoes'),
  processar: () => api.post<{ geradas: number; enviadas: number }>('/notificacoes/processar'),
}

// ---------- Portal do cliente ----------

const TOKEN_CLIENTE = 'vetcare.cliente.token'

export const apiCliente = axios.create({ baseURL: '/api/cliente', timeout: 15000 })

apiCliente.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_CLIENTE)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

apiCliente.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !String(error.config?.url ?? '').includes('/auth/login')) {
      localStorage.removeItem(TOKEN_CLIENTE)
      if (!window.location.pathname.startsWith('/cliente/login')) {
        window.location.assign('/cliente/login')
      }
    }
    return Promise.reject(error)
  },
)

export interface PetPayload {
  nome: string
  especie: string
  raca?: string | null
  sexo?: string | null
  dataNascimento?: string | null
}

export const clienteApi = {
  login: (cpf: string, senha: string) =>
    apiCliente.post<{
      token: string
      tipo: string
      expiraEmSegundos: number
      tutor: Tutor
      primeiroAcesso: boolean
    }>('/auth/login', { cpf, senha }),
  me: () => apiCliente.get<Tutor>('/auth/me'),
  alterarSenha: (senhaAtual: string, novaSenha: string) =>
    apiCliente.put('/auth/senha', { senhaAtual, novaSenha }),
  listarPets: () => apiCliente.get<Animal[]>('/pets'),
  criarPet: (payload: PetPayload) => apiCliente.post<Animal>('/pets', payload),
  atualizarPet: (id: number, payload: PetPayload) => apiCliente.put<Animal>(`/pets/${id}`, payload),
  listarSolicitacoes: () => apiCliente.get<SolicitacaoAgendamento[]>('/solicitacoes'),
  solicitar: (payload: { animalId: number; dataPreferida: string; motivo?: string | null }) =>
    apiCliente.post<SolicitacaoAgendamento>('/solicitacoes', payload),
}

export const solicitacoesApi = {
  listar: (status?: string) =>
    api.get<SolicitacaoAgendamento[]>('/solicitacoes', { params: { status } }),
  aprovar: (id: number, payload: { veterinarioId: number; dataHora: string; observacao?: string | null }) =>
    api.post<SolicitacaoAgendamento>(`/solicitacoes/${id}/aprovar`, payload),
  recusar: (id: number, motivo: string) =>
    api.post<SolicitacaoAgendamento>(`/solicitacoes/${id}/recusar`, { motivo }),
}

export function limparTokenCliente() {
  localStorage.removeItem(TOKEN_CLIENTE)
}

export function tokenCliente(): string | null {
  return localStorage.getItem(TOKEN_CLIENTE)
}

export const auditoriaApi = {
  listar: (params: {
    inicio?: string
    fim?: string
    acao?: string
    usuario?: string
    page?: number
    size?: number
  }) => api.get<PageResponse<Auditoria>>('/auditoria', { params }),
}
