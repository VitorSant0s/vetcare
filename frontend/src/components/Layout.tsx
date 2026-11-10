import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import {
  BarChart3,
  BellRing,
  Inbox,
  ScrollText,
  CalendarDays,
  LayoutDashboard,
  LogOut,
  Menu,
  PawPrint,
  FileText,
  ShieldCheck,
  Stethoscope,
  Users,
  Wallet,
  X,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import type { Perfil } from '../types'
import { clsx } from 'clsx'
import { useAuth } from '../context/AuthContext'
import { Badge } from './ui'

const navegacao: { para: string; rotulo: string; icone: LucideIcon; papeis?: Perfil[] }[] = [
  { para: '/portal', rotulo: 'Início', icone: LayoutDashboard },
  { para: '/agenda', rotulo: 'Agenda', icone: CalendarDays },
  { para: '/tutores', rotulo: 'Tutores', icone: Users },
  { para: '/animais', rotulo: 'Animais', icone: PawPrint, papeis: ['ADMINISTRADOR', 'RECEPCIONISTA'] },
  { para: '/pacientes', rotulo: 'Meus pacientes', icone: PawPrint, papeis: ['VETERINARIO'] },
  { para: '/prontuarios', rotulo: 'Prontuários', icone: FileText, papeis: ['VETERINARIO'] },
  { para: '/veterinarios', rotulo: 'Veterinários', icone: Stethoscope },
  { para: '/solicitacoes', rotulo: 'Solicitações', icone: Inbox, papeis: ['ADMINISTRADOR', 'RECEPCIONISTA'] },
  { para: '/financeiro', rotulo: 'Financeiro', icone: Wallet, papeis: ['ADMINISTRADOR', 'RECEPCIONISTA'] },
  { para: '/relatorios', rotulo: 'Relatórios', icone: BarChart3, papeis: ['ADMINISTRADOR', 'RECEPCIONISTA'] },
  { para: '/notificacoes', rotulo: 'Notificações', icone: BellRing, papeis: ['ADMINISTRADOR', 'RECEPCIONISTA'] },
  { para: '/usuarios', rotulo: 'Usuários', icone: ShieldCheck, papeis: ['ADMINISTRADOR'] },
  { para: '/auditoria', rotulo: 'Auditoria', icone: ScrollText, papeis: ['ADMINISTRADOR'] },
]

export function Layout() {
  const { usuario, sair } = useAuth()
  const [menuAberto, setMenuAberto] = useState(false)

  const itens = navegacao.filter((item) => !item.papeis || (usuario && item.papeis.includes(usuario.perfil)))

  const conteudoSidebar = (
    <>
      <Link to="/portal" className="flex items-center gap-2.5 px-5 py-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-600 text-white">
          <Stethoscope className="h-5 w-5" />
        </div>
        <div>
          <p className="text-base font-semibold text-slate-900">VetCare</p>
          <p className="text-[11px] tracking-wide text-slate-400 uppercase">Clínica veterinária</p>
        </div>
      </Link>
      <nav className="mt-2 flex-1 space-y-1 px-3">
        {itens.map(({ para, rotulo, icone: Icone }) => (
          <NavLink
            key={para}
            to={para}
            onClick={() => setMenuAberto(false)}
            className={({ isActive }) =>
              clsx(
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition',
                isActive
                  ? 'bg-brand-50 text-brand-700'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
              )
            }
          >
            <Icone className="h-4 w-4" />
            {rotulo}
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-slate-100 p-4">
        <div className="mb-3 flex items-start gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-slate-100 text-sm font-semibold text-slate-600">
            {usuario?.nome.charAt(0).toUpperCase()}
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-slate-800">{usuario?.nome}</p>
            <p className="truncate text-xs text-slate-400">{usuario?.email}</p>
            <div className="mt-1.5">
              <Badge tom={usuario?.perfil === 'ADMINISTRADOR' ? 'azul' : usuario?.perfil === 'VETERINARIO' ? 'verde' : 'ambar'}>
                {usuario?.perfilDescricao}
              </Badge>
            </div>
          </div>
        </div>
        <button
          onClick={sair}
          className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-slate-500 transition hover:bg-slate-100 hover:text-slate-800"
        >
          <LogOut className="h-4 w-4" />
          Sair do sistema
        </button>
      </div>
    </>
  )

  return (
    <div className="min-h-screen bg-slate-50 lg:flex">
      <aside className="hidden w-64 shrink-0 flex-col border-r border-slate-200 bg-white lg:flex">
        {conteudoSidebar}
      </aside>

      {menuAberto && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-slate-900/40" onClick={() => setMenuAberto(false)} />
          <aside className="relative flex h-full w-72 flex-col bg-white shadow-xl">
            <button
              onClick={() => setMenuAberto(false)}
              className="absolute top-4 right-4 rounded-lg p-1 text-slate-400 hover:bg-slate-100"
              aria-label="Fechar menu"
            >
              <X className="h-5 w-5" />
            </button>
            {conteudoSidebar}
          </aside>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex items-center gap-3 border-b border-slate-200 bg-white px-4 py-3 lg:hidden">
          <button
            onClick={() => setMenuAberto(true)}
            className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100"
            aria-label="Abrir menu"
          >
            <Menu className="h-5 w-5" />
          </button>
          <div className="flex items-center gap-2">
            <Stethoscope className="h-5 w-5 text-brand-600" />
            <span className="font-semibold text-slate-900">VetCare</span>
          </div>
        </header>

        <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:px-6 lg:py-8">
          <Outlet />
        </main>

        <footer className="px-6 py-4 text-center text-xs text-slate-400">
          VetCare · Engenharia de Software 1 · UFBA 2026
        </footer>
      </div>
    </div>
  )
}
