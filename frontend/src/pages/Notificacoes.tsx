import { useCallback, useEffect, useState } from 'react'
import { BellRing, Mail, RefreshCw, Send } from 'lucide-react'
import { useToast } from '../components/Toast'
import { mensagemDeErro, notificacoesApi } from '../lib/api'
import { formatarDataHora } from '../lib/format'
import type { Notificacao } from '../types'
import { Badge, Botao, CabecalhoPagina, Carregando, Cartao, Vazio } from '../components/ui'

export function Notificacoes() {
  const { mostrar } = useToast()
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([])
  const [carregando, setCarregando] = useState(true)
  const [processando, setProcessando] = useState(false)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await notificacoesApi.listar()
      setNotificacoes(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [mostrar])

  useEffect(() => {
    carregar()
  }, [carregar])

  async function processar() {
    setProcessando(true)
    try {
      const { data } = await notificacoesApi.processar()
      mostrar(`${data.geradas} avisos gerados e ${data.enviadas} enviados.`)
      await carregar()
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setProcessando(false)
    }
  }

  const enviadas = notificacoes.filter((n) => n.status === 'ENVIADA').length
  const pendentes = notificacoes.filter((n) => n.status === 'PENDENTE').length

  return (
    <>
      <CabecalhoPagina
        titulo="Notificações"
        descricao="Avisos ao tutor sobre consultas agendadas e próximas doses de vacina."
        acao={
          <Botao onClick={processar} carregando={processando}>
            <Send className="h-4 w-4" /> Gerar e enviar avisos
          </Botao>
        }
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
            <Mail className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Enviadas</p>
            <p className="text-xl font-semibold text-slate-900">{enviadas}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
            <RefreshCw className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Pendentes</p>
            <p className="text-xl font-semibold text-slate-900">{pendentes}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 text-sky-700">
            <BellRing className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Total</p>
            <p className="text-xl font-semibold text-slate-900">{notificacoes.length}</p>
          </div>
        </Cartao>
      </div>

      <Cartao>
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
          <h2 className="text-sm font-semibold text-slate-800">Caixa de saída</h2>
          <span className="text-xs text-slate-400">Canal: e-mail simulado (ADR-15)</span>
        </div>
        {carregando ? (
          <Carregando />
        ) : notificacoes.length === 0 ? (
          <Vazio
            titulo="Nenhum aviso gerado"
            descricao="Clique em “Gerar e enviar avisos” para processar consultas das próximas 24h e vacinas dos próximos 7 dias."
          />
        ) : (
          <ul className="divide-y divide-slate-100">
            {notificacoes.map((notificacao) => (
              <li key={notificacao.id} className="flex flex-col gap-2 px-4 py-3 sm:flex-row sm:items-start">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-brand-50 text-brand-700">
                  <Mail className="h-4 w-4" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-medium text-slate-800">{notificacao.tutorNome}</p>
                    <Badge tom={notificacao.tipo === 'CONSULTA' ? 'azul' : 'verde'}>
                      {notificacao.tipo}
                    </Badge>
                    <span className="text-xs text-slate-400">{notificacao.destinatario}</span>
                  </div>
                  <p className="mt-1 text-sm text-slate-600">{notificacao.mensagem}</p>
                  <p className="mt-1 text-xs text-slate-400">
                    Criada em {formatarDataHora(notificacao.criadoEm)}
                    {notificacao.enviadoEm && ` · enviada em ${formatarDataHora(notificacao.enviadoEm)}`}
                  </p>
                </div>
                <Badge
                  tom={
                    notificacao.status === 'ENVIADA'
                      ? 'verde'
                      : notificacao.status === 'FALHA'
                        ? 'vermelho'
                        : 'ambar'
                  }
                >
                  {notificacao.status}
                </Badge>
              </li>
            ))}
          </ul>
        )}
      </Cartao>
    </>
  )
}
