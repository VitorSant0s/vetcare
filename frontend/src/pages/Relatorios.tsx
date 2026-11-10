import { useCallback, useEffect, useState, type ReactNode } from 'react'
import {
  BarChart3,
  Download,
  PawPrint,
  Receipt,
  Stethoscope,
  Syringe,
  TrendingUp,
} from 'lucide-react'
import { useToast } from '../components/Toast'
import { mensagemDeErro, relatoriosApi } from '../lib/api'
import { formatarMoeda, hojeIso } from '../lib/format'
import type { RelatorioResumo } from '../types'
import { Botao, CabecalhoPagina, Campo, Carregando, Cartao, Entrada } from '../components/ui'

export function Relatorios() {
  const { mostrar } = useToast()
  const [inicio, setInicio] = useState(() => `${hojeIso().slice(0, 7)}-01`)
  const [fim, setFim] = useState(hojeIso())
  const [dados, setDados] = useState<RelatorioResumo | null>(null)
  const [carregando, setCarregando] = useState(true)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await relatoriosApi.resumo(inicio, fim)
      setDados(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [inicio, fim, mostrar])

  useEffect(() => {
    carregar()
  }, [carregar])

  function exportarCsv() {
    if (!dados) return
    const linhas = [
      ['Indicador', 'Valor'],
      ['Período', `${dados.inicio} a ${dados.fim}`],
      ['Consultas no período', dados.consultasTotal],
      ['Consultas agendadas', dados.consultasAgendadas],
      ['Consultas realizadas', dados.consultasRealizadas],
      ['Consultas canceladas', dados.consultasCanceladas],
      ['Atendimentos', dados.atendimentos],
      ['Animais cadastrados', dados.animaisCadastrados],
      ['Animais novos no período', dados.animaisNovos],
      ['Vacinas aplicadas', dados.vacinasAplicadas],
      ['Total recebido (R$)', dados.totalRecebido.toFixed(2).replace('.', ',')],
      [],
      ['Forma de pagamento', 'Quantidade', 'Total (R$)'],
      ...dados.porForma.map((forma) => [
        forma.descricao,
        forma.quantidade,
        forma.total.toFixed(2).replace('.', ','),
      ]),
    ]
    const csv = linhas.map((linha) => linha.join(';')).join('\n')
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `vetcare-relatorio-${dados.inicio}-a-${dados.fim}.csv`
    link.click()
    URL.revokeObjectURL(url)
    mostrar('Relatório exportado em CSV.')
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Relatórios"
        descricao="Consolidação de atendimentos, animais, consultas e finanças."
        acao={
          <Botao onClick={exportarCsv} disabled={!dados}>
            <Download className="h-4 w-4" /> Exportar CSV
          </Botao>
        }
      />

      <Cartao className="mb-6 p-4">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Campo label="Início">
            <Entrada type="date" value={inicio} onChange={(e) => setInicio(e.target.value)} />
          </Campo>
          <Campo label="Fim">
            <Entrada type="date" value={fim} min={inicio} onChange={(e) => setFim(e.target.value)} />
          </Campo>
          <div className="flex items-end">
            <Botao
              variante="contorno"
              className="w-full"
              onClick={() => {
                setInicio(`${hojeIso().slice(0, 7)}-01`)
                setFim(hojeIso())
              }}
            >
              Mês atual
            </Botao>
          </div>
          <div className="flex items-end">
            <Botao variante="contorno" className="w-full" onClick={() => window.print()}>
              Imprimir
            </Botao>
          </div>
        </div>
      </Cartao>

      {carregando || !dados ? (
        <Cartao>
          <Carregando texto="Gerando relatório..." />
        </Cartao>
      ) : (
        <div className="space-y-6">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
            <Indicador icone={<CalendarIcon />} rotulo="Consultas" valor={dados.consultasTotal} />
            <Indicador icone={<Stethoscope className="h-5 w-5" />} rotulo="Atendimentos" valor={dados.atendimentos} />
            <Indicador icone={<PawPrint className="h-5 w-5" />} rotulo="Animais" valor={dados.animaisCadastrados} />
            <Indicador icone={<Syringe className="h-5 w-5" />} rotulo="Vacinas" valor={dados.vacinasAplicadas} />
            <Cartao className="p-4">
              <p className="flex items-center gap-1.5 text-xs text-slate-500">
                <TrendingUp className="h-3.5 w-3.5" /> Recebido
              </p>
              <p className="mt-1 text-lg font-semibold text-brand-700">{formatarMoeda(dados.totalRecebido)}</p>
            </Cartao>
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <Cartao>
              <div className="border-b border-slate-100 px-4 py-3">
                <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
                  <BarChart3 className="h-4 w-4 text-brand-600" /> Consultas por status
                </h2>
              </div>
              <div className="divide-y divide-slate-100">
                <Linha rotulo="Agendadas" valor={dados.consultasAgendadas} />
                <Linha rotulo="Realizadas" valor={dados.consultasRealizadas} />
                <Linha rotulo="Canceladas" valor={dados.consultasCanceladas} />
                <Linha rotulo="Total no período" valor={dados.consultasTotal} destaque />
              </div>
            </Cartao>

            <Cartao>
              <div className="border-b border-slate-100 px-4 py-3">
                <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
                  <Receipt className="h-4 w-4 text-brand-600" /> Financeiro por forma
                </h2>
              </div>
              {dados.porForma.length === 0 ? (
                <p className="px-4 py-8 text-center text-sm text-slate-500">Sem pagamentos no período.</p>
              ) : (
                <div className="divide-y divide-slate-100">
                  {dados.porForma.map((forma) => (
                    <div key={forma.forma} className="flex items-center justify-between px-4 py-3 text-sm">
                      <span className="text-slate-600">
                        {forma.descricao}{' '}
                        <span className="text-xs text-slate-500">({forma.quantidade})</span>
                      </span>
                      <span className="font-medium text-slate-800">{formatarMoeda(forma.total)}</span>
                    </div>
                  ))}
                </div>
              )}
            </Cartao>
          </div>

          <Cartao className="p-4">
            <p className="text-sm text-slate-500">
              Animais novos no período: <strong className="text-slate-800">{dados.animaisNovos}</strong>. O
              relatório considera o período de {dados.inicio.split('-').reverse().join('/')} a{' '}
              {dados.fim.split('-').reverse().join('/')}.
            </p>
          </Cartao>
        </div>
      )}
    </>
  )
}

function Indicador({
  icone,
  rotulo,
  valor,
}: {
  icone: ReactNode
  rotulo: string
  valor: number
}) {
  return (
    <Cartao className="p-4">
      <p className="flex items-center gap-1.5 text-xs text-slate-500">
        {icone} {rotulo}
      </p>
      <p className="mt-1 text-xl font-semibold text-slate-900">{valor}</p>
    </Cartao>
  )
}

function Linha({ rotulo, valor, destaque }: { rotulo: string; valor: number; destaque?: boolean }) {
  return (
    <div className="flex items-center justify-between px-4 py-3 text-sm">
      <span className="text-slate-600">{rotulo}</span>
      <span className={destaque ? 'font-semibold text-slate-900' : 'text-slate-700'}>{valor}</span>
    </div>
  )
}

function CalendarIcon() {
  return <BarChart3 className="h-5 w-5" />
}
