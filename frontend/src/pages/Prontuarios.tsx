import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { FileText, Pill, Stethoscope } from 'lucide-react'
import { useToast } from '../components/Toast'
import { mensagemDeErro, veterinariosApi } from '../lib/api'
import { formatarDataHora } from '../lib/format'
import type { Atendimento } from '../types'
import { Badge, Busca, CabecalhoPagina, Carregando, Cartao, Vazio } from '../components/ui'

export function Prontuarios() {
  const { mostrar } = useToast()
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
