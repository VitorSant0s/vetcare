import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { authApi } from '../lib/api'
import type { Usuario } from '../types'

const TOKEN_KEY = 'vetcare.token'

interface AuthContexto {
  usuario: Usuario | null
  carregando: boolean
  entrar: (email: string, senha: string) => Promise<Usuario>
  sair: () => void
}

const Contexto = createContext<AuthContexto | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) {
      setCarregando(false)
      return
    }
    authApi
      .me()
      .then(({ data }) => setUsuario(data))
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setCarregando(false))
  }, [])

  const entrar = useCallback(async (email: string, senha: string) => {
    const { data } = await authApi.login(email, senha)
    localStorage.setItem(TOKEN_KEY, data.token)
    setUsuario(data.usuario)
    return data.usuario
  }, [])

  const sair = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    setUsuario(null)
    window.location.assign('/login')
  }, [])

  const valor = useMemo(() => ({ usuario, carregando, entrar, sair }), [usuario, carregando, entrar, sair])

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>
}

export function useAuth(): AuthContexto {
  const contexto = useContext(Contexto)
  if (!contexto) throw new Error('useAuth deve ser usado dentro de AuthProvider')
  return contexto
}
