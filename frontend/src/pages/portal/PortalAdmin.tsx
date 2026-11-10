import { useEffect, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight,
  CalendarDays,
  PawPrint,
  ShieldCheck,
  Stethoscope,
  Users,
  UserCog,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { animaisApi, consultasApi, tutoresApi, usuariosApi, veterinariosApi } from '../../lib/api'
import { formatarHora, hojeIso } from '../../lib/format'
import type { Consulta, Veterinario } from '../../types'
import { Cartao, Carregando } from '../../components/ui'

export function PortalAdmin() {
  const { usuario } = useAuth()
  const [carregando, setCarregando] = useState(true)
  const [totais, setTotais] = useState({ tutores: 0, animais: 0, usuarios: 0 })
  const [veterinarios, setVeterinarios] = useState<Veterinario[]>([])
  const [hoje, setHoje] = useState<Consulta[]>([])

  useEffect(() => {
    Promise.all([
      tutoresApi.listar(undefined, 0, 1),
      animaisApi.listar(undefined, 0, 1),
      usuariosApi.listar(undefined, 0, 1),
      veterinariosApi.listar(true),
      consultasApi.listar(hojeIso(), hojeIso()),
    ])
      .then(([tutores, animais, usuarios, vets, consultas]) => {
        setTotais({
          tutores: tutores.data.totalElements,
          animais: animais.data.totalElements,
          usuarios: usuarios.data.totalElements,
        })
        setVeterinarios(vets.data)
        setHoje(consultas.data)
      })
      .finally(() => setCarregando(false))
  }, [])

  if (carregando) {
    return (
      <Cartao>
        <Carregando texto="Carregando visão geral..." />
      </Cartao>
    )
  }

  const ativos = veterinarios.filter((v) => v.ativo).length

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-slate-900 via-slate-800 to-sky-900 px-6 py-7 text-white">
        <div className="absolute -top-16 -right-10 h-48 w-48 rounded-full bg-sky-400/20 blur-2xl" />
        <div className="relative flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs font-medium tracking-widest text-sky-200 uppercase">Portal administrativo</p>
            <h1 className="mt-1 text-2xl font-semibold">Visão geral da clínica</h1>
            <p className="mt-1 text-sm text-slate-300">
              Olá, {usuario?.nome.split(' ')[0]}. Gestão de pessoas, cadastros e agenda em um só lugar.
            </p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/10">
            <ShieldCheck className="h-7 w-7" />
          </div>
        </div>
      </section>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Indicador icone={<Users className="h-5 w-5" />} rotulo="Tutores" valor={totais.tutores} cor="bg-sky-50 text-sky-700" />
        <Indicador icone={<PawPrint className="h-5 w-5" />} rotulo="Animais" valor={totais.animais} cor="bg-brand-50 text-brand-700" />
        <Indicador icone={<UserCog className="h-5 w-5" />} rotulo="Usuários" valor={totais.usuarios} cor="bg-violet-50 text-violet-700" />
        <Indicador icone={<Stethoscope className="h-5 w-5" />} rotulo="Veterinários ativos" valor={ativos} cor="bg-amber-50 text-amber-700" />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Cartao className="lg:col-span-2">
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
            <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
              <CalendarDays className="h-4 w-4 text-brand-600" /> Consultas de hoje
            </h2>
            <Link to="/agenda" className="text-xs font-medium text-brand-700 hover:underline">
              Ver agenda
            </Link>
          </div>
          {hoje.length === 0 ? (
            <p className="px-4 py-8 text-center text-sm text-slate-400">Nenhuma consulta hoje.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {hoje.slice(0, 6).map((consulta) => (
                <li key={consulta.id} className="flex items-center gap-3 px-4 py-3">
                  <span className="rounded-lg bg-brand-50 px-2 py-1 text-xs font-semibold text-brand-700">
                    {formatarHora(consulta.dataHora)}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-slate-800">{consulta.animalNome}</p>
                    <p className="truncate text-xs text-slate-400">
                      {consulta.tutorNome} · {consulta.veterinarioNome}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Cartao>

        <Cartao className="p-4">
          <h2 className="text-sm font-semibold text-slate-800">Administração</h2>
          <div className="mt-3 space-y-2">
            <Atalho para="/usuarios" rotulo="Gerenciar usuários" />
            <Atalho para="/veterinarios" rotulo="Gerenciar veterinários" />
            <Atalho para="/tutores" rotulo="Gerenciar tutores" />
            <Atalho para="/animais" rotulo="Gerenciar animais" />
            <Atalho para="/agenda?nova=1" rotulo="Agendar consulta" />
            <Atalho para="/solicitacoes" rotulo="Solicitações de agendamento" />
            <Atalho para="/financeiro" rotulo="Financeiro" />
            <Atalho para="/relatorios" rotulo="Relatórios" />
            <Atalho para="/notificacoes" rotulo="Notificações" />
            <Atalho para="/auditoria" rotulo="Trilha de auditoria" />
          </div>
        </Cartao>
      </div>
    </div>
  )
}

function Indicador({
  icone,
  rotulo,
  valor,
  cor,
}: {
  icone: ReactNode
  rotulo: string
  valor: number
  cor: string
}) {
  return (
    <Cartao className="flex items-center gap-3 p-4">
      <div className={`flex h-10 w-10 items-center justify-center rounded-xl ${cor}`}>{icone}</div>
      <div>
        <p className="text-xs text-slate-500">{rotulo}</p>
        <p className="text-xl font-semibold text-slate-900">{valor}</p>
      </div>
    </Cartao>
  )
}

function Atalho({ para, rotulo }: { para: string; rotulo: string }) {
  return (
    <Link
      to={para}
      className="flex items-center justify-between rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-600 transition hover:border-brand-200 hover:bg-brand-50 hover:text-brand-800"
    >
      {rotulo}
      <ArrowRight className="h-3.5 w-3.5" />
    </Link>
  )
}
