import axios from 'axios'
import type {
  Animal,
  Consulta,
  ErroApi,
  PageResponse,
  Tutor,
  Usuario,
  Veterinario,
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

export const tutoresApi = {
  listar: (termo?: string, page = 0, size = 20) =>
    api.get<PageResponse<Tutor>>('/tutores', { params: { termo, page, size } }),
  criar: (payload: Omit<Tutor, 'id' | 'criadoEm'>) => api.post<Tutor>('/tutores', payload),
  atualizar: (id: number, payload: Omit<Tutor, 'id' | 'criadoEm'>) =>
    api.put<Tutor>(`/tutores/${id}`, payload),
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
  criar: (payload: VeterinarioPayload) => api.post<Veterinario>('/veterinarios', payload),
  atualizar: (id: number, payload: VeterinarioPayload) =>
    api.put<Veterinario>(`/veterinarios/${id}`, payload),
  excluir: (id: number) => api.delete(`/veterinarios/${id}`),
}
