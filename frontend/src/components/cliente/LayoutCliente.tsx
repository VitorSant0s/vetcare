import { Link, Outlet, useLocation } from 'react-router-dom'
import { useEffect, useRef, useState } from 'react'
import { CalendarPlus, ClipboardList, LogOut, Menu, PawPrint, Stethoscope, X } from 'lucide-react'
import { clsx } from 'clsx'
import { useClienteAuth } from '../../context/ClienteAuthContext'
import { formatarCpf } from '../../lib/format'

const itens = [
  { aba: 'inicio', rotulo: 'Início', icone: Stethoscope, para: '/portal/cliente' },
  { aba: 'pets', rotulo: 'Meus pets', icone: PawPrint, para: '/portal/cliente?aba=pets' },
  { aba: 'agendar', rotulo: 'Agendar', icone: CalendarPlus, para: '/portal/cliente?aba=agendar' },
  { aba: 'pedidos', rotulo: 'Meus pedidos', icone: ClipboardList, para: '/portal/cliente?aba=pedidos' },
]

export function LayoutCliente() {
  const { tutor, sair } = useClienteAuth()
  const location = useLocation()
  const [menuAberto, setMenuAberto] = useState(false)
  const menuRef = useRef<HTMLElement>(null)
  const botaoMenuRef = useRef<HTMLButtonElement>(null)
  const abaAtual = new URLSearchParams(location.search).get('aba') ?? 'inicio'

  const conteudo = (
    <>
      <div className="flex items-center gap-2.5 px-5 py-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-600 text-white">
          <PawPrint className="h-5 w-5" />
        </div>
        <div>
          <p className="text-base font-semibold text-slate-900">VetCare</p>
          <p className="text-[11px] tracking-wide text-slate-500 uppercase">Portal do cliente</p>
        </div>
      </div>

      <nav className="mt-2 flex-1 space-y-1 px-3">
        {itens.map(({ aba, rotulo, icone: Icone, para }) => (
          <Link
            key={aba}
            to={para}
            onClick={() => setMenuAberto(false)}
            className={clsx(
              'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition',
              abaAtual === aba
                ? 'bg-brand-50 text-brand-700'
                : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
            )}
          >
            <Icone className="h-4 w-4" />
            {rotulo}
          </Link>
        ))}
      </nav>

      <div className="border-t border-slate-100 p-4">
        <div className="mb-3 flex items-center gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-50 text-sm font-semibold text-brand-700">
            {tutor?.nome.charAt(0).toUpperCase()}
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-slate-800">{tutor?.nome}</p>
            <p className="truncate text-xs text-slate-500">{tutor?.cpf ? formatarCpf(tutor.cpf) : ''}</p>
          </div>
        </div>
        <button
          onClick={sair}
          className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-slate-500 transition hover:bg-slate-100 hover:text-slate-800"
        >
          <LogOut className="h-4 w-4" /> Sair do portal
        </button>
      </div>
    </>
  )

  useEffect(() => {
    if (!menuAberto) return
    const focaveis = () =>
      Array.from(
        menuRef.current?.querySelectorAll<HTMLElement>('a[href], button:not([disabled])') ?? [],
      )
    window.setTimeout(() => focaveis()[0]?.focus(), 0)

    function aoTeclar(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        evento.preventDefault()
        setMenuAberto(false)
        return
      }
      if (evento.key !== 'Tab') return
      const elementos = focaveis()
      if (elementos.length === 0) return
      const primeiro = elementos[0]
      const ultimo = elementos[elementos.length - 1]
      if (evento.shiftKey && document.activeElement === primeiro) {
        evento.preventDefault()
        ultimo.focus()
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault()
        primeiro.focus()
      }
    }

    document.addEventListener('keydown', aoTeclar)
    return () => {
      document.removeEventListener('keydown', aoTeclar)
      botaoMenuRef.current?.focus()
    }
  }, [menuAberto])

  return (
    <div className="min-h-screen bg-slate-50 lg:flex">
      <a
        href="#conteudo"
        className="sr-only z-[70] rounded-lg bg-brand-700 px-4 py-2 text-sm font-medium text-white focus:not-sr-only focus:fixed focus:top-3 focus:left-3"
      >
        Pular para o conteúdo
      </a>
      <aside id="menu-principal" className="hidden w-64 shrink-0 flex-col border-r border-slate-200 bg-white lg:flex">
        {conteudo}
      </aside>

      {menuAberto && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-slate-900/40" onClick={() => setMenuAberto(false)} />
          <aside ref={menuRef} className="relative flex h-full w-72 flex-col bg-white shadow-xl">
            <button
              onClick={() => setMenuAberto(false)}
              className="absolute top-4 right-4 rounded-lg p-1 text-slate-500 hover:bg-slate-100"
              aria-label="Fechar menu"
            >
              <X className="h-5 w-5" />
            </button>
            {conteudo}
          </aside>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex items-center gap-3 border-b border-slate-200 bg-white px-4 py-3 lg:hidden">
          <button
            ref={botaoMenuRef}
            onClick={() => setMenuAberto(true)}
            className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100"
            aria-label="Abrir menu"
            aria-expanded={menuAberto}
            aria-controls="menu-principal"
          >
            <Menu className="h-5 w-5" />
          </button>
          <div className="flex items-center gap-2">
            <PawPrint className="h-5 w-5 text-brand-600" />
            <span className="font-semibold text-slate-900">VetCare Cliente</span>
          </div>
        </header>

        <main id="conteudo" className="mx-auto w-full max-w-5xl flex-1 px-4 py-6 sm:px-6 lg:py-8">
          <Outlet />
        </main>

        <footer className="px-6 py-4 text-center text-xs text-slate-500">
          VetCare · Portal do Cliente · UFBA 2026
        </footer>
      </div>
    </div>
  )
}
