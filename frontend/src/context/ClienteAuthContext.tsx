import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { clienteApi, limparTokenCliente, tokenCliente } from '../lib/api'
import type { Tutor } from '../types'

interface ClienteAuthContexto {
  tutor: Tutor | null
  carregando: boolean
  entrar: (cpf: string, senha: string) => Promise<Tutor>
  sair: () => void
  alterarSenha: (atual: string, nova: string) => Promise<void>
  marcarSenhaAlterada: () => void
  atualizarTutor: (tutor: Tutor) => void
}

const Contexto = createContext<ClienteAuthContexto | null>(null)

export function ClienteAuthProvider({ children }: { children: ReactNode }) {
  const [tutor, setTutor] = useState<Tutor | null>(null)
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    if (!tokenCliente()) {
      setCarregando(false)
      return
    }
    clienteApi
      .me()
      .then(({ data }) => setTutor(data))
      .catch(() => limparTokenCliente())
      .finally(() => setCarregando(false))
  }, [])

  const entrar = useCallback(async (cpf: string, senha: string) => {
    const { data } = await clienteApi.login(cpf, senha)
    localStorage.setItem('vetcare.cliente.token', data.token)
    setTutor(data.tutor)
    return data.tutor
  }, [])

  const sair = useCallback(async () => {
    await clienteApi.logout().catch(() => undefined)
    limparTokenCliente()
    setTutor(null)
    window.location.assign('/cliente/login')
  }, [])

  const alterarSenha = useCallback(async (atual: string, nova: string) => {
    if (!tutor) return
    await clienteApi.alterarSenha(atual, nova)
    const { data } = await clienteApi.login(tutor.cpf, nova)
    localStorage.setItem('vetcare.cliente.token', data.token)
    setTutor(data.tutor)
  }, [tutor])

  const atualizarTutor = useCallback((novo: Tutor) => {
    setTutor(novo)
  }, [])

  const marcarSenhaAlterada = useCallback(() => {
    setTutor((atual) => (atual ? { ...atual, primeiroAcesso: false } : atual))
  }, [])

  const valor = useMemo(
    () => ({ tutor, carregando, entrar, sair, alterarSenha, marcarSenhaAlterada, atualizarTutor }),
    [tutor, carregando, entrar, sair, alterarSenha, marcarSenhaAlterada, atualizarTutor],
  )

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>
}

export function useClienteAuth(): ClienteAuthContexto {
  const contexto = useContext(Contexto)
  if (!contexto) throw new Error('useClienteAuth deve ser usado dentro de ClienteAuthProvider')
  return contexto
}
