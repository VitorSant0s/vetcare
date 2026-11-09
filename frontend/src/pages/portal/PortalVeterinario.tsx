import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CalendarDays, Clock, PawPrint, Stethoscope, UserRound } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { consultasApi } from '../../lib/api'
import { daquiADiasIso, formatarHora, hojeIso } from '../../lib/format'
import type { Consulta } from '../../types'
import { Badge, Cartao, Carregando } from '../../components/ui'

export function PortalVeterinario() {
  const { usuario } = useAuth()
  const [carregando, setCarregando] = useState(true)
  const [hoje, setHoje] = useState<Consulta[]>([])
  const [semana, setSemana] = useState<Consulta[]>([])

  useEffect(() => {
    Promise.all([
      consultasApi.listar(hojeIso(), hojeIso()),
      consultasApi.listar(hojeIso(), daquiADiasIso(7)),
    ])
      .then(([hojeResp, semanaResp]) => {
        setHoje(hojeResp.data)
        setSemana(semanaResp.data)
      })
      .finally(() => setCarregando(false))
  }, [])

  if (carregando) {
    return (
      <Cartao>
        <Carregando texto="Carregando sua agenda..." />
      </Cartao>
    )
  }

  const proximas = semana.filter((c) => new Date(c.dataHora) >= new Date())

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-brand-800 via-brand-700 to-brand-500 px-6 py-7 text-white">
        <div className="absolute -top-16 -right-10 h-48 w-48 rounded-full bg-white/10 blur-2xl" />
        <div className="relative flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs font-medium tracking-widest text-brand-100 uppercase">Portal do veterinário</p>
            <h1 className="mt-1 text-2xl font-semibold">Sua agenda clínica</h1>
            <p className="mt-1 text-sm text-brand-50">
              Olá, {usuario?.nome}. Você vê apenas os atendimentos sob sua responsabilidade.
            </p>
            {usuario?.crmv && (
              <p className="mt-1 text-xs text-brand-200">
                {usuario.crmv} · {usuario.especialidade || 'Clínica geral'}
              </p>
            )}
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/15">
            <Stethoscope className="h-7 w-7" />
          </div>
        </div>
      </section>

      <div className="grid gap-3 sm:grid-cols-3">
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
            <CalendarDays className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Hoje</p>
            <p className="text-xl font-semibold text-slate-900">{hoje.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 text-sky-700">
            <Clock className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Próximas (7 dias)</p>
            <p className="text-xl font-semibold text-slate-900">{proximas.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
            <PawPrint className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Total na semana</p>
            <p className="text-xl font-semibold text-slate-900">{semana.length}</p>
          </div>
        </Cartao>
      </div>

      <Cartao>
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
          <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
            <CalendarDays className="h-4 w-4 text-brand-600" /> Consultas de hoje
          </h2>
          <Link to="/agenda" className="text-xs font-medium text-brand-700 hover:underline">
            Ver agenda completa
          </Link>
        </div>
        {hoje.length === 0 ? (
          <p className="px-4 py-10 text-center text-sm text-slate-400">
            Você não tem consultas agendadas para hoje.
          </p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {hoje.map((consulta) => (
              <li key={consulta.id} className="flex flex-col gap-2 px-4 py-3 sm:flex-row sm:items-center">
                <span className="w-16 rounded-lg bg-brand-50 px-2 py-1 text-center text-xs font-semibold text-brand-700">
                  {formatarHora(consulta.dataHora)}
                </span>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-slate-800">{consulta.animalNome}</p>
                  <p className="text-xs text-slate-400">
                    <UserRound className="mr-1 inline h-3 w-3" /> {consulta.tutorNome}
                  </p>
                  {consulta.observacoes && (
                    <p className="mt-1 text-xs text-slate-500">{consulta.observacoes}</p>
                  )}
                </div>
                <Badge tom={consulta.status === 'AGENDADA' ? 'verde' : 'neutro'}>
                  {consulta.status}
                </Badge>
              </li>
            ))}
          </ul>
        )}
      </Cartao>
    </div>
  )
}
