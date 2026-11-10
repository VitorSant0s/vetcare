import { useCallback, useEffect, useState } from 'react'
import { Activity, RefreshCw, ShieldCheck } from 'lucide-react'
import { useToast } from '../components/Toast'
import { auditoriaApi, mensagemDeErro } from '../lib/api'
import { formatarDataHora, hojeIso, somarDiasIso } from '../lib/format'
import type { Auditoria } from '../types'
import {
  Badge,
  Botao,
  CabecalhoPagina,
  Campo,
  Carregando,
  Cartao,
  Entrada,
  Paginacao,
  Vazio,
} from '../components/ui'

function tomAcao(acao: string): 'verde' | 'vermelho' | 'ambar' | 'azul' | 'neutro' {
  if (acao.includes('FALHA') || acao.includes('NEGADO') || acao.includes('INVALIDO') || acao.includes('EXPIRADO') || acao.includes('REVOGADO')) {
    return 'vermelho'
  }
  if (acao.includes('CRIADO') || acao.includes('AGENDADA') || acao.includes('REGISTRADO') || acao.includes('SUCESSO') || acao.includes('APROVADA') || acao.includes('ENVIADA')) {
    return 'verde'
  }
  if (acao.includes('ATUALIZADO') || acao.includes('ALTERADA') || acao.includes('CANCELADA') || acao.includes('RECUSADA')) {
    return 'ambar'
  }
  if (acao.includes('ACESSO') || acao.includes('CONSULTAD') || acao.includes('COMPARTILHADO')) {
    return 'azul'
  }
  return 'neutro'
}

export function AuditoriaPage() {
  const { mostrar } = useToast()
  const [inicio, setInicio] = useState(somarDiasIso(hojeIso(), -7))
  const [fim, setFim] = useState(hojeIso())
  const [acao, setAcao] = useState('')
  const [usuario, setUsuario] = useState('')
  const [pagina, setPagina] = useState(0)
  const [dados, setDados] = useState<{ content: Auditoria[]; page: number; totalPages: number; totalElements: number } | null>(null)
  const [carregando, setCarregando] = useState(true)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await auditoriaApi.listar({
        inicio: inicio || undefined,
        fim: fim || undefined,
        acao: acao || undefined,
        usuario: usuario || undefined,
        page: pagina,
        size: 25,
      })
      setDados(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [inicio, fim, acao, usuario, pagina, mostrar])

  useEffect(() => {
    const timer = window.setTimeout(carregar, 250)
    return () => window.clearTimeout(timer)
  }, [carregar])

  return (
    <>
      <CabecalhoPagina
        titulo="Auditoria"
        descricao="Trilha imutável de logins, acessos e alterações em dados sensíveis."
        acao={
          <Botao variante="contorno" onClick={carregar}>
            <RefreshCw className="h-4 w-4" /> Atualizar
          </Botao>
        }
      />

      <Cartao className="mb-6 p-4">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          <Campo label="Início">
            <Entrada type="date" value={inicio} onChange={(e) => { setInicio(e.target.value); setPagina(0) }} />
          </Campo>
          <Campo label="Fim">
            <Entrada type="date" value={fim} min={inicio} onChange={(e) => { setFim(e.target.value); setPagina(0) }} />
          </Campo>
          <Campo label="Ação">
            <Entrada
              placeholder="Ex.: LOGIN, PRONTUARIO"
              value={acao}
              onChange={(e) => { setAcao(e.target.value.toUpperCase()); setPagina(0) }}
            />
          </Campo>
          <Campo label="Usuário">
            <Entrada
              placeholder="e-mail ou CPF"
              value={usuario}
              onChange={(e) => { setUsuario(e.target.value); setPagina(0) }}
            />
          </Campo>
          <div className="flex items-end">
            <Botao
              variante="contorno"
              className="w-full"
              onClick={() => {
                setInicio(somarDiasIso(hojeIso(), -7))
                setFim(hojeIso())
                setAcao('')
                setUsuario('')
                setPagina(0)
              }}
            >
              Limpar filtros
            </Botao>
          </div>
        </div>
      </Cartao>

      <Cartao>
        <div className="flex flex-col gap-2 border-b border-slate-100 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
          <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
            <ShieldCheck className="h-4 w-4 text-brand-600" /> Registros
          </h2>
          <span className="text-xs text-slate-500">
            {dados?.totalElements ?? 0} eventos no filtro
          </span>
        </div>

        {carregando ? (
          <Carregando />
        ) : !dados || dados.content.length === 0 ? (
          <Vazio
            titulo="Nenhum evento encontrado"
            descricao="Ajuste o período ou os filtros de ação/usuário."
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
                  <tr>
                    <th className="px-4 py-2 font-medium">Data/hora</th>
                    <th className="px-4 py-2 font-medium">Usuário</th>
                    <th className="px-4 py-2 font-medium">Ação</th>
                    <th className="px-4 py-2 font-medium">Entidade</th>
                    <th className="px-4 py-2 font-medium">Detalhes</th>
                    <th className="px-4 py-2 font-medium">IP</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {dados.content.map((evento) => (
                    <tr key={evento.id} className="hover:bg-slate-50/60">
                      <td className="px-4 py-2 whitespace-nowrap text-slate-500">
                        {formatarDataHora(evento.criadoEm)}
                      </td>
                      <td className="px-4 py-2 text-slate-600">{evento.usuarioEmail || '—'}</td>
                      <td className="px-4 py-2">
                        <Badge tom={tomAcao(evento.acao)}>{evento.acao}</Badge>
                      </td>
                      <td className="px-4 py-2 text-slate-500">
                        {evento.entidade || '—'}
                        {evento.entidadeId != null && ` #${evento.entidadeId}`}
                      </td>
                      <td className="max-w-80 px-4 py-2 text-slate-500">
                        <span className="line-clamp-2">{evento.detalhes || '—'}</span>
                      </td>
                      <td className="px-4 py-2 text-slate-500">{evento.ip || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Paginacao
              pagina={dados.page}
              totalPaginas={dados.totalPages}
              totalElementos={dados.totalElements}
              onAnterior={() => setPagina((p) => Math.max(0, p - 1))}
              onProxima={() => setPagina((p) => p + 1)}
            />
          </>
        )}
      </Cartao>

      <div className="mt-4 flex items-center gap-2 text-xs text-slate-500">
        <Activity className="h-3.5 w-3.5" /> A consulta da própria trilha também é registrada.
      </div>
    </>
  )
}
