import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { FileText, PawPrint, Stethoscope } from 'lucide-react'
import { useToast } from '../components/Toast'
import { mensagemDeErro, veterinariosApi } from '../lib/api'
import { formatarDataHora, idade, statusProximaDose } from '../lib/format'
import type { PacienteClinico } from '../types'
import { Badge, Busca, CabecalhoPagina, Carregando, Cartao, Vazio } from '../components/ui'

export function Pacientes() {
  const { mostrar } = useToast()
  const [pacientes, setPacientes] = useState<PacienteClinico[]>([])
  const [termo, setTermo] = useState('')
  const [carregando, setCarregando] = useState(true)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await veterinariosApi.meusPacientes()
      setPacientes(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [mostrar])

  useEffect(() => {
    carregar()
  }, [carregar])

  const filtrados = useMemo(() => {
    const busca = termo.trim().toLowerCase()
    if (!busca) return pacientes
    return pacientes.filter(
      (paciente) =>
        paciente.animal.nome.toLowerCase().includes(busca) ||
        paciente.animal.tutorNome.toLowerCase().includes(busca),
    )
  }, [pacientes, termo])

  const vacinasAVencer = pacientes.filter((p) => {
    if (!p.proximaVacina) return false
    const dias = Math.round(
      (new Date(`${p.proximaVacina}T00:00:00`).getTime() -
        new Date(`${new Date().toISOString().slice(0, 10)}T00:00:00`).getTime()) /
        86400000,
    )
    return dias <= 30
  }).length

  return (
    <>
      <CabecalhoPagina
        titulo="Meus pacientes"
        descricao="Animais que já tiveram consulta com você. Abra o prontuário para o histórico completo."
      />

      <div className="mb-4 grid gap-3 sm:grid-cols-3">
        <Cartao className="p-4">
          <p className="text-xs text-slate-500">Pacientes</p>
          <p className="text-2xl font-semibold text-slate-900">{pacientes.length}</p>
        </Cartao>
        <Cartao className="p-4">
          <p className="text-xs text-slate-500">Vacinas a vencer (30 dias)</p>
          <p className="text-2xl font-semibold text-amber-700">{vacinasAVencer}</p>
        </Cartao>
        <Cartao className="p-4">
          <p className="text-xs text-slate-500">Consultas registradas</p>
          <p className="text-2xl font-semibold text-slate-900">
            {pacientes.reduce((soma, p) => soma + p.totalConsultas, 0)}
          </p>
        </Cartao>
      </div>

      <Cartao>
        <div className="border-b border-slate-100 p-4">
          <Busca valor={termo} onChange={setTermo} placeholder="Buscar por paciente ou tutor" />
        </div>

        {carregando ? (
          <Carregando />
        ) : filtrados.length === 0 ? (
          <Vazio
            titulo="Nenhum paciente encontrado"
            descricao={
              termo
                ? 'Tente outro termo de busca.'
                : 'Pacientes aparecem aqui após uma consulta vinculada a você.'
            }
          />
        ) : (
          <div className="divide-y divide-slate-100">
            {filtrados.map((paciente) => {
              const dose = statusProximaDose(paciente.proximaVacina)
              return (
                <div key={paciente.animal.id} className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center">
                  <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                    <PawPrint className="h-5 w-5" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="font-medium text-slate-800">{paciente.animal.nome}</p>
                      <Badge tom="verde">{paciente.animal.especie}</Badge>
                      {paciente.vacinaAtrasada && <Badge tom="vermelho">Vacina atrasada</Badge>}
                    </div>
                    <p className="text-xs text-slate-500">
                      {paciente.animal.raca || 'Raça não informada'} · {idade(paciente.animal.dataNascimento)} · Tutor(a):{' '}
                      {paciente.animal.tutorNome}
                    </p>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm sm:w-72">
                    <div>
                      <p className="flex items-center gap-1 text-xs text-slate-400">
                        <Stethoscope className="h-3.5 w-3.5" /> Última consulta
                      </p>
                      <p className="text-slate-700">
                        {paciente.ultimaConsulta ? formatarDataHora(paciente.ultimaConsulta) : '—'}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-slate-400">Próxima vacina</p>
                      <p className="text-slate-700">
                        {dose.texto}
                        {paciente.tipoVacina && <span className="block text-xs text-slate-400">{paciente.tipoVacina}</span>}
                      </p>
                    </div>
                  </div>
                  <Link
                    to={`/animais/${paciente.animal.id}/prontuario`}
                    className="inline-flex items-center gap-1.5 rounded-lg bg-brand-600 px-3 py-2 text-xs font-medium text-white hover:bg-brand-700"
                  >
                    <FileText className="h-3.5 w-3.5" /> Prontuário
                  </Link>
                </div>
              )
            })}
          </div>
        )}
      </Cartao>
    </>
  )
}
