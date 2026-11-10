import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  CalendarDays,
  ClipboardList,
  FileText,
  PawPrint,
  Pill,
  Stethoscope,
  Syringe,
  UserRound,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { consultasApi, veterinariosApi } from '../../lib/api'
import { daquiADiasIso, formatarHora, hojeIso, statusProximaDose } from '../../lib/format'
import type { Atendimento, Consulta, PacienteClinico } from '../../types'
import { Badge, Cartao, Carregando } from '../../components/ui'

export function PortalVeterinario() {
  const { usuario } = useAuth()
  const [carregando, setCarregando] = useState(true)
  const [hoje, setHoje] = useState<Consulta[]>([])
  const [semana, setSemana] = useState<Consulta[]>([])
  const [pacientes, setPacientes] = useState<PacienteClinico[]>([])
  const [atendimentos, setAtendimentos] = useState<Atendimento[]>([])

  useEffect(() => {
    Promise.all([
      consultasApi.listar(hojeIso(), hojeIso()),
      consultasApi.listar(hojeIso(), daquiADiasIso(7)),
      veterinariosApi.meusPacientes(),
      veterinariosApi.meusAtendimentos(),
    ])
      .then(([hojeResp, semanaResp, pacientesResp, atendimentosResp]) => {
        setHoje(hojeResp.data)
        setSemana(semanaResp.data)
        setPacientes(pacientesResp.data)
        setAtendimentos(atendimentosResp.data)
      })
      .finally(() => setCarregando(false))
  }, [])

  if (carregando) {
    return (
      <Cartao>
        <Carregando texto="Carregando área médica..." />
      </Cartao>
    )
  }

  const proximas = semana.filter((c) => new Date(c.dataHora) >= new Date())
  const vacinasAVencer = pacientes
    .filter((p) => {
      if (!p.proximaVacina) return false
      const dias = Math.round(
        (new Date(`${p.proximaVacina}T00:00:00`).getTime() -
          new Date(`${hojeIso()}T00:00:00`).getTime()) /
          86400000,
      )
      return dias <= 30
    })
    .sort((a, b) => (a.proximaVacina! > b.proximaVacina! ? 1 : -1))

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-brand-800 via-brand-700 to-brand-500 px-6 py-7 text-white">
        <div className="absolute -top-16 -right-10 h-48 w-48 rounded-full bg-white/10 blur-2xl" />
        <div className="relative flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs font-medium tracking-widest text-brand-100 uppercase">Área médica veterinária</p>
            <h1 className="mt-1 text-2xl font-semibold">Sua rotina clínica</h1>
            <p className="mt-1 text-sm text-brand-50">
              Olá, {usuario?.nome}. Agenda, pacientes e prontuários em um só lugar.
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

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
            <CalendarDays className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Consultas hoje</p>
            <p className="text-xl font-semibold text-slate-900">{hoje.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 text-sky-700">
            <PawPrint className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Meus pacientes</p>
            <p className="text-xl font-semibold text-slate-900">{pacientes.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
            <Syringe className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Vacinas a vencer</p>
            <p className="text-xl font-semibold text-slate-900">{vacinasAVencer.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-50 text-violet-700">
            <ClipboardList className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Atendimentos</p>
            <p className="text-xl font-semibold text-slate-900">{atendimentos.length}</p>
          </div>
        </Cartao>
      </div>

      <div className="flex flex-wrap gap-2">
        <Link
          to="/agenda"
          className="inline-flex items-center gap-1.5 rounded-lg bg-brand-600 px-3 py-2 text-sm font-medium text-white hover:bg-brand-700"
        >
          <CalendarDays className="h-4 w-4" /> Minha agenda
        </Link>
        <Link
          to="/pacientes"
          className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 hover:bg-slate-50"
        >
          <PawPrint className="h-4 w-4" /> Meus pacientes
        </Link>
        <Link
          to="/prontuarios"
          className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 hover:bg-slate-50"
        >
          <FileText className="h-4 w-4" /> Prontuários
        </Link>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Cartao className="lg:col-span-2">
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
                  </div>
                  <Link
                    to={`/animais/${consulta.animalId}/prontuario`}
                    className="text-xs font-medium text-brand-700 hover:underline"
                  >
                    Prontuário
                  </Link>
                  <Badge tom={consulta.status === 'AGENDADA' ? 'verde' : 'azul'}>{consulta.status}</Badge>
                </li>
              ))}
            </ul>
          )}
        </Cartao>

        <Cartao>
          <div className="border-b border-slate-100 px-4 py-3">
            <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
              <Syringe className="h-4 w-4 text-amber-600" /> Vacinas a vencer
            </h2>
          </div>
          {vacinasAVencer.length === 0 ? (
            <p className="px-4 py-8 text-center text-sm text-slate-400">Nenhuma dose nos próximos 30 dias.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {vacinasAVencer.slice(0, 5).map((paciente) => {
                const status = statusProximaDose(paciente.proximaVacina)
                return (
                  <li key={paciente.animal.id} className="flex items-center gap-2 px-4 py-3">
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-medium text-slate-800">{paciente.animal.nome}</p>
                      <p className="text-xs text-slate-400">{paciente.tipoVacina || 'Vacina'}</p>
                    </div>
                    <Badge tom={status.tom}>{status.texto}</Badge>
                  </li>
                )
              })}
            </ul>
          )}
        </Cartao>
      </div>

      <Cartao>
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
          <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
            <ClipboardList className="h-4 w-4 text-brand-600" /> Últimos atendimentos
          </h2>
          <div className="flex items-center gap-3">
            <span className="text-xs text-slate-400">
              {proximas.length} consultas nos próximos 7 dias
            </span>
            <Link to="/prontuarios" className="text-xs font-medium text-brand-700 hover:underline">
              Ver todos
            </Link>
          </div>
        </div>
        {atendimentos.length === 0 ? (
          <p className="px-4 py-8 text-center text-sm text-slate-400">
            Nenhum atendimento registrado. Registre pela agenda.
          </p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {atendimentos.slice(0, 5).map((atendimento) => (
              <li key={atendimento.id} className="flex flex-col gap-2 px-4 py-3 sm:flex-row sm:items-center">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-sky-50 text-sky-700">
                  <Stethoscope className="h-4 w-4" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-slate-800">{atendimento.animalNome}</p>
                  <p className="text-xs text-slate-500">{atendimento.diagnostico}</p>
                </div>
                {atendimento.prescricoes.length > 0 && (
                  <span className="flex items-center gap-1 text-xs text-slate-400">
                    <Pill className="h-3.5 w-3.5" /> {atendimento.prescricoes.length}
                  </span>
                )}
                <Link
                  to={`/animais/${atendimento.animalId}/prontuario`}
                  className="text-xs font-medium text-brand-700 hover:underline"
                >
                  Abrir prontuário
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Cartao>
    </div>
  )
}
