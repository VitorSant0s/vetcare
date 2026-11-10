import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { CalendarCheck, CalendarDays, Inbox, XCircle } from 'lucide-react'
import { useToast } from '../components/Toast'
import { mensagemDeErro, solicitacoesApi, veterinariosApi } from '../lib/api'
import { formatarDataHora, paraInputDateTime } from '../lib/format'
import type { SolicitacaoAgendamento, Veterinario } from '../types'
import {
  Badge,
  Botao,
  CabecalhoPagina,
  Campo,
  Carregando,
  Cartao,
  Entrada,
  Modal,
  Selecao,
  Vazio,
} from '../components/ui'

const schema = z.object({
  veterinarioId: z.string().min(1, 'Selecione o veterinário'),
  dataHora: z.string().min(1, 'Informe a data e o horário'),
  observacao: z.string().optional(),
})

type Formulario = z.infer<typeof schema>

export function Solicitacoes() {
  const { mostrar } = useToast()
  const [solicitacoes, setSolicitacoes] = useState<SolicitacaoAgendamento[]>([])
  const [veterinarios, setVeterinarios] = useState<Veterinario[]>([])
  const [carregando, setCarregando] = useState(true)
  const [aprovando, setAprovando] = useState<SolicitacaoAgendamento | null>(null)
  const [recusando, setRecusando] = useState<SolicitacaoAgendamento | null>(null)
  const [motivoRecusa, setMotivoRecusa] = useState('')
  const [salvando, setSalvando] = useState(false)
  const [erroApi, setErroApi] = useState('')

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Formulario>({ resolver: zodResolver(schema) })

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await solicitacoesApi.listar()
      setSolicitacoes(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [mostrar])

  useEffect(() => {
    carregar()
    veterinariosApi.listar().then(({ data }) => setVeterinarios(data)).catch(() => setVeterinarios([]))
  }, [carregar])

  function abrirAprovacao(solicitacao: SolicitacaoAgendamento) {
    setErroApi('')
    setAprovando(solicitacao)
    reset({
      veterinarioId: veterinarios[0] ? String(veterinarios[0].id) : '',
      dataHora: paraInputDateTime(new Date(solicitacao.dataPreferida)),
      observacao: `Pedido do cliente: ${solicitacao.motivo ?? 'sem observação'}`,
    })
  }

  async function aprovar(dados: Formulario) {
    if (!aprovando) return
    setSalvando(true)
    setErroApi('')
    try {
      await solicitacoesApi.aprovar(aprovando.id, {
        veterinarioId: Number(dados.veterinarioId),
        dataHora: dados.dataHora.length === 16 ? `${dados.dataHora}:00` : dados.dataHora,
        observacao: dados.observacao?.trim() || null,
      })
      mostrar('Solicitação aprovada e consulta agendada.')
      setAprovando(null)
      await carregar()
    } catch (error) {
      setErroApi(mensagemDeErro(error))
    } finally {
      setSalvando(false)
    }
  }

  async function recusar() {
    if (!recusando) return
    if (!motivoRecusa.trim()) {
      setErroApi('Informe o motivo da recusa.')
      return
    }
    setSalvando(true)
    setErroApi('')
    try {
      await solicitacoesApi.recusar(recusando.id, motivoRecusa.trim())
      mostrar('Solicitação recusada.')
      setRecusando(null)
      setMotivoRecusa('')
      await carregar()
    } catch (error) {
      setErroApi(mensagemDeErro(error))
    } finally {
      setSalvando(false)
    }
  }

  const pendentes = solicitacoes.filter((s) => s.status === 'PENDENTE')
  const historico = solicitacoes.filter((s) => s.status !== 'PENDENTE')

  return (
    <>
      <CabecalhoPagina
        titulo="Solicitações"
        descricao="Pedidos de agendamento enviados pelos clientes no portal."
        acao={
          <Badge tom={pendentes.length > 0 ? 'ambar' : 'verde'}>
            {pendentes.length} {pendentes.length === 1 ? 'pendente' : 'pendentes'}
          </Badge>
        }
      />

      <Cartao className="mb-6">
        <div className="flex items-center gap-2 border-b border-slate-100 px-4 py-3">
          <Inbox className="h-4 w-4 text-amber-600" />
          <h2 className="text-sm font-semibold text-slate-800">Aguardando análise</h2>
        </div>
        {carregando ? (
          <Carregando />
        ) : pendentes.length === 0 ? (
          <Vazio titulo="Nenhum pedido pendente" descricao="Novos pedidos do portal aparecem aqui." />
        ) : (
          <ul className="divide-y divide-slate-100">
            {pendentes.map((solicitacao) => (
              <li key={solicitacao.id} className="flex flex-col gap-3 px-4 py-4 lg:flex-row lg:items-center">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
                  <CalendarDays className="h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-slate-800">
                    {solicitacao.animalNome} · {solicitacao.tutorNome}
                  </p>
                  <p className="text-xs text-slate-500">
                    Preferência: {formatarDataHora(solicitacao.dataPreferida)}
                  </p>
                  {solicitacao.motivo && <p className="text-xs text-slate-500">{solicitacao.motivo}</p>}
                </div>
                <div className="flex gap-2">
                  <Botao variante="contorno" className="px-3 py-1.5 text-xs" onClick={() => { setErroApi(''); setRecusando(solicitacao); setMotivoRecusa('') }}>
                    <XCircle className="h-3.5 w-3.5" /> Recusar
                  </Botao>
                  <Botao className="px-3 py-1.5 text-xs" onClick={() => abrirAprovacao(solicitacao)}>
                    <CalendarCheck className="h-3.5 w-3.5" /> Aprovar
                  </Botao>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Cartao>

      <Cartao>
        <div className="border-b border-slate-100 px-4 py-3">
          <h2 className="text-sm font-semibold text-slate-800">Histórico</h2>
        </div>
        {historico.length === 0 ? (
          <p className="px-4 py-8 text-center text-sm text-slate-500">Nenhuma solicitação analisada.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
                <tr>
                  <th className="px-4 py-2 font-medium">Pet / Tutor</th>
                  <th className="px-4 py-2 font-medium">Preferência</th>
                  <th className="px-4 py-2 font-medium">Status</th>
                  <th className="px-4 py-2 font-medium">Resposta</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {historico.map((solicitacao) => (
                  <tr key={solicitacao.id}>
                    <td className="px-4 py-2">
                      <p className="font-medium text-slate-800">{solicitacao.animalNome}</p>
                      <p className="text-xs text-slate-500">{solicitacao.tutorNome}</p>
                    </td>
                    <td className="px-4 py-2 text-slate-500">{formatarDataHora(solicitacao.dataPreferida)}</td>
                    <td className="px-4 py-2">
                      <Badge tom={solicitacao.status === 'APROVADA' ? 'verde' : 'vermelho'}>
                        {solicitacao.status}
                      </Badge>
                    </td>
                    <td className="px-4 py-2 text-slate-500">
                      {solicitacao.observacaoResposta || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Cartao>

      <Modal
        aberto={!!aprovando}
        onFechar={() => setAprovando(null)}
        titulo="Aprovar solicitação"
        descricao={
          aprovando
            ? `${aprovando.animalNome} · ${aprovando.tutorNome} · preferência ${formatarDataHora(aprovando.dataPreferida)}`
            : undefined
        }
      >
        <form onSubmit={handleSubmit(aprovar)} className="space-y-4" noValidate>
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Veterinário" erro={errors.veterinarioId?.message} obrigatorio>
              <Selecao {...register('veterinarioId')}>
                <option value="">Selecione</option>
                {veterinarios.map((vet) => (
                  <option key={vet.id} value={vet.id}>
                    {vet.nome}
                  </option>
                ))}
              </Selecao>
            </Campo>
            <Campo label="Data e horário confirmados" erro={errors.dataHora?.message} obrigatorio>
              <Entrada type="datetime-local" {...register('dataHora')} />
            </Campo>
          </div>
          <Campo label="Observação para o cliente" erro={errors.observacao?.message}>
            <Entrada placeholder="Opcional" maxLength={500} {...register('observacao')} />
          </Campo>
          {erroApi && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {erroApi}
            </div>
          )}
          <div className="flex justify-end gap-2">
            <Botao type="button" variante="contorno" onClick={() => setAprovando(null)}>
              Cancelar
            </Botao>
            <Botao type="submit" carregando={salvando}>
              Confirmar agendamento
            </Botao>
          </div>
        </form>
      </Modal>

      <Modal
        aberto={!!recusando}
        onFechar={() => setRecusando(null)}
        titulo="Recusar solicitação"
        descricao={recusando ? `${recusando.animalNome} · ${recusando.tutorNome}` : undefined}
        largura="max-w-md"
      >
        <div className="space-y-4">
          <Campo label="Motivo da recusa" obrigatorio>
            <Entrada
              value={motivoRecusa}
              onChange={(e) => setMotivoRecusa(e.target.value)}
              placeholder="Ex.: horário indisponível"
              maxLength={500}
            />
          </Campo>
          {erroApi && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {erroApi}
            </div>
          )}
          <div className="flex justify-end gap-2">
            <Botao variante="contorno" onClick={() => setRecusando(null)}>
              Cancelar
            </Botao>
            <Botao variante="perigo" onClick={recusar} carregando={salvando}>
              Recusar pedido
            </Botao>
          </div>
        </div>
      </Modal>
    </>
  )
}
