import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { CheckCircle2, Info, XCircle } from 'lucide-react'

type TipoToast = 'sucesso' | 'erro' | 'info'

interface Toast {
  id: number
  tipo: TipoToast
  mensagem: string
}

interface ToastContexto {
  mostrar: (mensagem: string, tipo?: TipoToast) => void
}

const Contexto = createContext<ToastContexto | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const mostrar = useCallback((mensagem: string, tipo: TipoToast = 'sucesso') => {
    const id = Date.now() + Math.random()
    setToasts((atuais) => [...atuais, { id, tipo, mensagem }])
  }, [])

  const remover = useCallback((id: number) => {
    setToasts((atuais) => atuais.filter((t) => t.id !== id))
  }, [])

  const valor = useMemo(() => ({ mostrar }), [mostrar])

  return (
    <Contexto.Provider value={valor}>
      {children}
      <div className="pointer-events-none fixed top-4 right-4 z-[60] flex w-full max-w-sm flex-col gap-2">
        {toasts.map((toast) => (
          <ToastItem key={toast.id} toast={toast} onFechar={() => remover(toast.id)} />
        ))}
      </div>
    </Contexto.Provider>
  )
}

function ToastItem({ toast, onFechar }: { toast: Toast; onFechar: () => void }) {
  useEffect(() => {
    const timer = window.setTimeout(onFechar, 4000)
    return () => window.clearTimeout(timer)
  }, [onFechar])

  const estilos = {
    sucesso: 'border-brand-200 bg-white text-brand-800',
    erro: 'border-red-200 bg-white text-red-800',
    info: 'border-sky-200 bg-white text-sky-800',
  }
  const Icone = toast.tipo === 'sucesso' ? CheckCircle2 : toast.tipo === 'erro' ? XCircle : Info

  return (
    <div
      className={`pointer-events-auto flex items-start gap-3 rounded-xl border px-4 py-3 text-sm shadow-lg ${estilos[toast.tipo]}`}
      role="status" aria-live="polite"
    >
      <Icone className="mt-0.5 h-4 w-4 shrink-0" />
      <p className="flex-1">{toast.mensagem}</p>
      <button onClick={onFechar} className="text-slate-500 hover:text-slate-600" aria-label="Fechar aviso">
        ×
      </button>
    </div>
  )
}

export function useToast(): ToastContexto {
  const contexto = useContext(Contexto)
  if (!contexto) throw new Error('useToast deve ser usado dentro de ToastProvider')
  return contexto
}
