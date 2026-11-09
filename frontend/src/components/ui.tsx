import type {
  ButtonHTMLAttributes,
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from 'react'
import { clsx } from 'clsx'
import { AlertTriangle, Loader2, Search, X } from 'lucide-react'

type Variante = 'primario' | 'secundario' | 'contorno' | 'perigo' | 'fantasma'

const variantes: Record<Variante, string> = {
  primario: 'bg-brand-600 text-white hover:bg-brand-700 shadow-sm',
  secundario: 'bg-brand-50 text-brand-700 hover:bg-brand-100',
  contorno: 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50',
  perigo: 'bg-red-600 text-white hover:bg-red-700 shadow-sm',
  fantasma: 'text-slate-600 hover:bg-slate-100',
}

interface BotaoProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: Variante
  carregando?: boolean
  children: ReactNode
}

export function Botao({
  variante = 'primario',
  carregando = false,
  className,
  children,
  disabled,
  ...props
}: BotaoProps) {
  return (
    <button
      className={clsx(
        'inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition',
        'disabled:cursor-not-allowed disabled:opacity-60',
        variantes[variante],
        className,
      )}
      disabled={disabled || carregando}
      {...props}
    >
      {carregando && <Loader2 className="h-4 w-4 animate-spin" />}
      {children}
    </button>
  )
}

interface CampoProps {
  label: string
  erro?: string
  obrigatorio?: boolean
  children: ReactNode
  className?: string
}

export function Campo({ label, erro, obrigatorio, children, className }: CampoProps) {
  return (
    <label className={clsx('block', className)}>
      <span className="mb-1.5 block text-sm font-medium text-slate-700">
        {label}
        {obrigatorio && <span className="text-red-500"> *</span>}
      </span>
      {children}
      {erro && <span className="mt-1 block text-xs text-red-600">{erro}</span>}
    </label>
  )
}

const estiloInput =
  'w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 shadow-sm placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100 focus:outline-none disabled:bg-slate-100'

export function Entrada(props: InputHTMLAttributes<HTMLInputElement>) {
  return <input {...props} className={clsx(estiloInput, props.className)} />
}

export function AreaTexto(props: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea {...props} className={clsx(estiloInput, 'min-h-24 resize-y', props.className)} />
}

export function Selecao(props: SelectHTMLAttributes<HTMLSelectElement>) {
  return <select {...props} className={clsx(estiloInput, 'pr-8', props.className)} />
}

export function Busca({
  valor,
  onChange,
  placeholder = 'Buscar...',
}: {
  valor: string
  onChange: (valor: string) => void
  placeholder?: string
}) {
  return (
    <div className="relative w-full sm:max-w-xs">
      <Search className="pointer-events-none absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-slate-400" />
      <input
        value={valor}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className={clsx(estiloInput, 'pl-9')}
      />
    </div>
  )
}

export function Badge({
  children,
  tom = 'neutro',
}: {
  children: ReactNode
  tom?: 'verde' | 'azul' | 'ambar' | 'vermelho' | 'neutro'
}) {
  const tons = {
    verde: 'bg-brand-50 text-brand-700 ring-brand-200',
    azul: 'bg-sky-50 text-sky-700 ring-sky-200',
    ambar: 'bg-amber-50 text-amber-700 ring-amber-200',
    vermelho: 'bg-red-50 text-red-700 ring-red-200',
    neutro: 'bg-slate-100 text-slate-600 ring-slate-200',
  }
  return (
    <span
      className={clsx(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset',
        tons[tom],
      )}
    >
      {children}
    </span>
  )
}

export function Cartao({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <div className={clsx('rounded-xl border border-slate-200 bg-white shadow-sm', className)}>
      {children}
    </div>
  )
}

export function CabecalhoPagina({
  titulo,
  descricao,
  acao,
}: {
  titulo: string
  descricao?: string
  acao?: ReactNode
}) {
  return (
    <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-slate-900">{titulo}</h1>
        {descricao && <p className="mt-1 text-sm text-slate-500">{descricao}</p>}
      </div>
      {acao}
    </div>
  )
}

export function Vazio({
  titulo,
  descricao,
  acao,
}: {
  titulo: string
  descricao: string
  acao?: ReactNode
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 px-6 py-16 text-center">
      <div className="mb-2 flex h-12 w-12 items-center justify-center rounded-full bg-slate-100">
        <Search className="h-5 w-5 text-slate-400" />
      </div>
      <p className="font-medium text-slate-700">{titulo}</p>
      <p className="max-w-sm text-sm text-slate-500">{descricao}</p>
      {acao && <div className="mt-3">{acao}</div>}
    </div>
  )
}

export function Carregando({ texto = 'Carregando...' }: { texto?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-12 text-sm text-slate-500">
      <Loader2 className="h-4 w-4 animate-spin" />
      {texto}
    </div>
  )
}

export function Modal({
  aberto,
  onFechar,
  titulo,
  descricao,
  children,
  largura = 'max-w-lg',
}: {
  aberto: boolean
  onFechar: () => void
  titulo: string
  descricao?: string
  children: ReactNode
  largura?: string
}) {
  if (!aberto) return null
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-slate-900/40 p-0 backdrop-blur-sm sm:items-center sm:p-4">
      <div
        className={clsx(
          'max-h-[92vh] w-full overflow-y-auto rounded-t-2xl bg-white shadow-xl sm:rounded-2xl',
          largura,
        )}
      >
        <div className="flex items-start justify-between border-b border-slate-100 px-6 py-4">
          <div>
            <h2 className="text-lg font-semibold text-slate-900">{titulo}</h2>
            {descricao && <p className="mt-0.5 text-sm text-slate-500">{descricao}</p>}
          </div>
          <button
            onClick={onFechar}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            aria-label="Fechar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="px-6 py-5">{children}</div>
      </div>
    </div>
  )
}

export function Confirmacao({
  aberto,
  titulo,
  mensagem,
  onConfirmar,
  onCancelar,
  carregando,
}: {
  aberto: boolean
  titulo: string
  mensagem: string
  onConfirmar: () => void
  onCancelar: () => void
  carregando?: boolean
}) {
  return (
    <Modal aberto={aberto} onFechar={onCancelar} titulo={titulo} largura="max-w-md">
      <div className="flex gap-3">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-red-50">
          <AlertTriangle className="h-5 w-5 text-red-600" />
        </div>
        <p className="text-sm text-slate-600">{mensagem}</p>
      </div>
      <div className="mt-6 flex justify-end gap-2">
        <Botao variante="contorno" onClick={onCancelar} disabled={carregando}>
          Cancelar
        </Botao>
        <Botao variante="perigo" onClick={onConfirmar} carregando={carregando}>
          Confirmar exclusão
        </Botao>
      </div>
    </Modal>
  )
}

export function Paginacao({
  pagina,
  totalPaginas,
  totalElementos,
  onAnterior,
  onProxima,
}: {
  pagina: number
  totalPaginas: number
  totalElementos: number
  onAnterior: () => void
  onProxima: () => void
}) {
  return (
    <div className="flex flex-col gap-2 border-t border-slate-100 px-4 py-3 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between">
      <span>
        {totalElementos} {totalElementos === 1 ? 'registro' : 'registros'}
      </span>
      <div className="flex items-center gap-2">
        <Botao
          variante="contorno"
          className="px-3 py-1"
          onClick={onAnterior}
          disabled={pagina <= 0}
        >
          Anterior
        </Botao>
        <span className="px-1">
          {totalPaginas === 0 ? 0 : pagina + 1} de {totalPaginas}
        </span>
        <Botao
          variante="contorno"
          className="px-3 py-1"
          onClick={onProxima}
          disabled={pagina + 1 >= totalPaginas}
        >
          Próxima
        </Botao>
      </div>
    </div>
  )
}
