import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  CalendarDays,
  CalendarPlus,
  PawPrint,
  BellRing,
  Plus,
  UserRound,
  Users,
  Wallet,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { animaisApi, consultasApi, tutoresApi } from '../../lib/api'
import { daquiADiasIso, formatarHora, hojeIso } from '../../lib/format'
import type { Consulta } from '../../types'
import { Cartao, Carregando } from '../../components/ui'

export function PortalRecepcao() {
  const { usuario } = useAuth()
  const [carregando, setCarregando] = useState(true)
  const [hoje, setHoje] = useState<Consulta[]>([])
  const [proximas, setProximas] = useState<Consulta[]>([])
  const [totais, setTotais] = useState({ tutores: 0, animais: 0 })

  useEffect(() => {
    Promise.all([
      consultasApi.listar(hojeIso(), hojeIso()),
      consultasApi.listar(hojeIso(), daquiADiasIso(7)),
      tutoresApi.listar(undefined, 0, 1),
      animaisApi.listar(undefined, 0, 1),
    ])
      .then(([hojeResp, proximasResp, tutores, animais]) => {
        setHoje(hojeResp.data)
        setProximas(proximasResp.data)
        setTotais({ tutores: tutores.data.totalElements, animais: animais.data.totalElements })
      })
      .finally(() => setCarregando(false))
  }, [])

  if (carregando) {
    return (
      <Cartao>
        <Carregando texto="Carregando portal da recepção..." />
      </Cartao>
    )
  }

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-amber-500 via-amber-600 to-brand-700 px-6 py-7 text-white">
        <div className="absolute -top-16 -right-10 h-48 w-48 rounded-full bg-white/15 blur-2xl" />
        <div className="relative flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs font-medium tracking-widest text-amber-100 uppercase">Portal da recepção</p>
            <h1 className="mt-1 text-2xl font-semibold">Atendimento e agenda do dia</h1>
            <p className="mt-1 text-sm text-amber-50">
              Olá, {usuario?.nome.split(' ')[0]}. Organize a chegada dos tutores e os horários da clínica.
            </p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/15">
            <CalendarDays className="h-7 w-7" />
          </div>
        </div>
      </section>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Cartao className="p-4">
          <p className="text-xs text-slate-500">Consultas hoje</p>
          <p className="text-2xl font-semibold text-slate-900">{hoje.length}</p>
        </Cartao>
        <Cartao className="p-4">
          <p className="text-xs text-slate-500">Próximos 7 dias</p>
          <p className="text-2xl font-semibold text-slate-900">{proximas.length}</p>
        </Cartao>
        <Cartao className="p-4">
          <p className="text-xs text-slate-500">Tutores</p>
          <p className="text-2xl font-semibold text-slate-900">{totais.tutores}</p>
        </Cartao>
        <Cartao className="p-4">
          <p className="text-xs text-slate-500">Animais</p>
          <p className="text-2xl font-semibold text-slate-900">{totais.animais}</p>
        </Cartao>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Cartao className="lg:col-span-2">
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
            <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
              <CalendarDays className="h-4 w-4 text-amber-600" /> Agenda de hoje
            </h2>
            <Link to="/agenda" className="text-xs font-medium text-brand-700 hover:underline">
              Abrir agenda
            </Link>
          </div>
          {hoje.length === 0 ? (
            <div className="px-4 py-10 text-center">
              <p className="text-sm text-slate-500">Nenhuma consulta para hoje.</p>
              <Link
                to="/agenda?nova=1"
                className="mt-3 inline-flex items-center gap-1.5 text-sm font-medium text-brand-700 hover:underline"
              >
                <Plus className="h-4 w-4" /> Agendar consulta
              </Link>
            </div>
          ) : (
            <ul className="divide-y divide-slate-100">
              {hoje.map((consulta) => (
                <li key={consulta.id} className="flex items-center gap-3 px-4 py-3">
                  <span className="rounded-lg bg-amber-50 px-2 py-1 text-xs font-semibold text-amber-700">
                    {formatarHora(consulta.dataHora)}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-slate-800">{consulta.animalNome}</p>
                    <p className="truncate text-xs text-slate-400">
                      <UserRound className="mr-1 inline h-3 w-3" />
                      {consulta.tutorNome} · {consulta.veterinarioNome}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Cartao>

        <Cartao className="p-4">
          <h2 className="text-sm font-semibold text-slate-800">Ações rápidas</h2>
          <div className="mt-3 space-y-2">
            <Link
              to="/agenda?nova=1"
              className="flex items-center gap-2 rounded-lg bg-brand-600 px-3 py-2.5 text-sm font-medium text-white hover:bg-brand-700"
            >
              <CalendarPlus className="h-4 w-4" /> Agendar consulta
            </Link>
            <Link
              to="/tutores"
              className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
            >
              <Users className="h-4 w-4 text-slate-400" /> Cadastrar tutor
            </Link>
            <Link
              to="/animais"
              className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
            >
              <PawPrint className="h-4 w-4 text-slate-400" /> Cadastrar animal
            </Link>
            <Link
              to="/solicitacoes"
              className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
            >
              <CalendarPlus className="h-4 w-4 text-slate-400" /> Solicitações
            </Link>
            <Link
              to="/financeiro"
              className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
            >
              <Wallet className="h-4 w-4 text-slate-400" /> Financeiro
            </Link>
            <Link
              to="/notificacoes"
              className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
            >
              <BellRing className="h-4 w-4 text-slate-400" /> Notificações
            </Link>
          </div>
        </Cartao>
      </div>
    </div>
  )
}
