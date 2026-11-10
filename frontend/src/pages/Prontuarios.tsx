import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { FileText, Pill, Search, Stethoscope } from 'lucide-react'
import { useToast } from '../components/Toast'
import { mensagemDeErro, veterinariosApi } from '../lib/api'
import { formatarDataHora } from '../lib/format'
import type { Atendimento } from '../types'
import { Badge, Busca, CabecalhoPagina, Carregando, Cartao, Vazio } from '../components/ui'

export function Prontuarios() {
  const { mostrar } = useToast()
  const navigate = useNavigate()
  const [codigo, setCodigo] = useState('')
  const [atendimentos, setAtendimentos] = useState<Atendimento[]>([])
  const [termo, setTermo] = useState('')
  const [carregando, setCarregando] = useState(true)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await veterinariosApi.meusAtendimentos()
      setAtendimentos(data)
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
    if (!busca) return atendimentos
    return atendimentos.filter(
      (atendimento) =>
        atendimento.animalNome.toLowerCase().includes(busca) ||
        atendimento.diagnostico.toLowerCase().includes(busca),
    )
  }, [atendimentos, termo])

  return (
    <>
      <CabecalhoPagina
        titulo="Prontuários"
        descricao="Atendimentos que você registrou, com diagnóstico e prescrições."
        acao={
          <Link
            to="/pacientes"
            className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Ver pacientes
          </Link>
        }
      />

      <Cartao className="mb-6 p-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="flex-1">
            <p className="mb-1.5 text-sm font-medium text-slate-700">Buscar prontuário por código</p>
            <div className="relative">
              <Search className="pointer-events-none absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                value={codigo}
                onChange={(e) => setCodigo(e.target.value.toUpperCase())}
                placeholder="Ex.: ABC7DEFG"
                maxLength={12}
                className="w-full rounded-lg border border-slate-300 bg-white py-2 pr-3 pl-9 font-mono text-sm tracking-widest text-slate-800 uppercase shadow-sm placeholder:font-sans placeholder:tracking-normal placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100 focus:outline-none"
              />
            </div>
          </div>
          <button
            onClick={() => {
              if (codigo.trim()) navigate(`/prontuarios/compartilhado/${codigo.trim()}`)
            }}
            className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
            disabled={!codigo.trim()}
          >
            Buscar prontuário
          </button>
        </div>
        <p className="mt-2 text-xs text-slate-400">
          Códigos são gerados ao compartilhar um prontuário e valem por 7 dias.
        </p>
      </Cartao>

      <Cartao>
        <div className="border-b border-slate-100 p-4">
          <Busca valor={termo} onChange={setTermo} placeholder="Buscar por paciente ou diagnóstico" />
        </div>

        {carregando ? (
          <Carregando />
        ) : filtrados.length === 0 ? (
          <Vazio
            titulo="Nenhum atendimento registrado"
            descricao="Os atendimentos que você registrar na agenda aparecem aqui."
            acao={
              <Link to="/agenda" className="text-sm font-medium text-brand-700 hover:underline">
                Abrir minha agenda
              </Link>
            }
          />
        ) : (
          <div className="divide-y divide-slate-100">
            {filtrados.map((atendimento) => (
              <div key={atendimento.id} className="flex flex-col gap-3 p-4 lg:flex-row lg:items-center">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-sky-50 text-sky-700">
                  <Stethoscope className="h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="font-medium text-slate-800">{atendimento.animalNome}</p>
                    <Badge tom="azul">{formatarDataHora(atendimento.dataHora)}</Badge>
                    {atendimento.prescricoes.length > 0 && (
                      <span className="inline-flex items-center gap-1 text-xs text-slate-400">
                        <Pill className="h-3.5 w-3.5" /> {atendimento.prescricoes.length} prescrição(ões)
                      </span>
                    )}
                  </div>
                  <p className="mt-1 text-sm text-slate-600">{atendimento.diagnostico}</p>
                  {atendimento.observacoes && (
                    <p className="text-xs text-slate-400">{atendimento.observacoes}</p>
                  )}
                </div>
                <Link
                  to={`/animais/${atendimento.animalId}/prontuario`}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50"
                >
                  <FileText className="h-3.5 w-3.5" /> Abrir prontuário
                </Link>
              </div>
            ))}
          </div>
        )}
      </Cartao>
    </>
  )
}
