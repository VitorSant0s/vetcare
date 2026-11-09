import { useCallback, useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Banknote, CalendarDays, CreditCard, Plus, Receipt, TrendingUp } from 'lucide-react'
import { useToast } from '../components/Toast'
import { consultasApi, mensagemDeErro, pagamentosApi, tutoresApi } from '../lib/api'
import { formatarData, formatarMoeda, hojeIso, somarDiasIso } from '../lib/format'
import type { Consulta, DashboardFinanceiro, Pagamento, Tutor } from '../types'
import {
  Badge,
  Botao,
  Campo,
  Carregando,
  Cartao,
  CabecalhoPagina,
  Entrada,
  Modal,
  Selecao,
  Vazio,
} from '../components/ui'

const schema = z.object({
  tutorId: z.string().min(1, 'Selecione o tutor'),
  consultaId: z.string().optional(),
  valor: z.string().min(1, 'Informe o valor'),
  formaPagamento: z.enum(['DINHEIRO', 'PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'CONVENIO']),
  dataPagamento: z.string().min(1, 'Informe a data'),
  status: z.enum(['PAGO', 'PENDENTE', 'CANCELADO']),
  observacoes: z.string().optional(),
})

type Formulario = z.infer<typeof schema>

const formas = [
  ['DINHEIRO', 'Dinheiro'],
  ['PIX', 'Pix'],
  ['CARTAO_CREDITO', 'Cartão de crédito'],
  ['CARTAO_DEBITO', 'Cartão de débito'],
  ['CONVENIO', 'Convênio'],
] as const

const vazio: Formulario = {
  tutorId: '',
  consultaId: '',
  valor: '',
  formaPagamento: 'PIX',
  dataPagamento: hojeIso(),
  status: 'PAGO',
  observacoes: '',
}

export function Financeiro() {
  const { mostrar } = useToast()
  const [inicio, setInicio] = useState(() => `${hojeIso().slice(0, 7)}-01`)
  const [fim, setFim] = useState(hojeIso())
  const [dashboard, setDashboard] = useState<DashboardFinanceiro | null>(null)
  const [pagamentos, setPagamentos] = useState<Pagamento[]>([])
  const [tutores, setTutores] = useState<Tutor[]>([])
  const [consultas, setConsultas] = useState<Consulta[]>([])
  const [carregando, setCarregando] = useState(true)
  const [modalAberto, setModalAberto] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [erroApi, setErroApi] = useState('')

  const {
    register,
    handleSubmit,
    watch,
    reset,
    setValue,
    formState: { errors },
  } = useForm<Formulario>({ resolver: zodResolver(schema), defaultValues: vazio })

  const tutorSelecionado = watch('tutorId')
  const consultasDoTutor = useMemo(
    () => consultas.filter((c) => String(c.tutorId) === tutorSelecionado),
    [consultas, tutorSelecionado],
  )

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const [dash, lista] = await Promise.all([
        pagamentosApi.dashboard(inicio, fim),
        pagamentosApi.listar(inicio, fim),
      ])
      setDashboard(dash.data)
      setPagamentos(lista.data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [inicio, fim, mostrar])

  useEffect(() => {
    carregar()
  }, [carregar])

  useEffect(() => {
    tutoresApi.listar(undefined, 0, 100).then(({ data }) => setTutores(data.content)).catch(() => [])
    consultasApi
      .listar(somarDiasIso(hojeIso(), -90), hojeIso())
      .then(({ data }) => setConsultas(data))
      .catch(() => [])
  }, [])

  async function abrirModal() {
    setErroApi('')
    reset(vazio)
    if (tutores.length === 0) {
      const { data } = await tutoresApi.listar(undefined, 0, 100)
      setTutores(data.content)
    }
    setModalAberto(true)
  }

  async function salvar(dados: Formulario) {
    setSalvando(true)
    setErroApi('')
    try {
      await pagamentosApi.criar({
        tutorId: Number(dados.tutorId),
        consultaId: dados.consultaId ? Number(dados.consultaId) : null,
        valor: Number(dados.valor.replace(',', '.')),
        formaPagamento: dados.formaPagamento,
        dataPagamento: dados.dataPagamento,
        status: dados.status,
        observacoes: dados.observacoes?.trim() || null,
      })
      mostrar('Pagamento registrado.')
      setModalAberto(false)
      await carregar()
    } catch (error) {
      setErroApi(mensagemDeErro(error))
    } finally {
      setSalvando(false)
    }
  }

  const maiorForma = Math.max(1, ...(dashboard?.porForma.map((f) => f.total) ?? [1]))

  return (
    <>
      <CabecalhoPagina
        titulo="Financeiro"
        descricao="Pagamentos recebidos e indicadores da clínica."
        acao={
          <Botao onClick={abrirModal}>
            <Plus className="h-4 w-4" /> Registrar pagamento
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
        </div>
      </Cartao>

      {carregando ? (
        <Cartao>
          <Carregando />
        </Cartao>
      ) : (
        <>
          <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Cartao className="p-4">
              <p className="flex items-center gap-1.5 text-xs text-slate-500">
                <TrendingUp className="h-3.5 w-3.5" /> Total recebido
              </p>
              <p className="mt-1 text-2xl font-semibold text-brand-700">
                {formatarMoeda(dashboard?.totalRecebido ?? 0)}
              </p>
            </Cartao>
            <Cartao className="p-4">
              <p className="flex items-center gap-1.5 text-xs text-slate-500">
                <Receipt className="h-3.5 w-3.5" /> Pagamentos
              </p>
              <p className="mt-1 text-2xl font-semibold text-slate-900">
                {dashboard?.quantidadePagamentos ?? 0}
              </p>
            </Cartao>
            <Cartao className="p-4">
              <p className="flex items-center gap-1.5 text-xs text-slate-500">
                <Banknote className="h-3.5 w-3.5" /> Ticket médio
              </p>
              <p className="mt-1 text-2xl font-semibold text-slate-900">
                {formatarMoeda(dashboard?.ticketMedio ?? 0)}
              </p>
            </Cartao>
            <Cartao className="p-4">
              <p className="flex items-center gap-1.5 text-xs text-slate-500">
                <CalendarDays className="h-3.5 w-3.5" /> Atendimentos
              </p>
              <p className="mt-1 text-2xl font-semibold text-slate-900">{dashboard?.atendimentos ?? 0}</p>
            </Cartao>
          </div>

          <div className="grid gap-4 lg:grid-cols-3">
            <Cartao className="p-4 lg:col-span-1">
              <h2 className="flex items-center gap-1.5 text-sm font-semibold text-slate-800">
                <CreditCard className="h-4 w-4 text-brand-600" /> Por forma de pagamento
              </h2>
              {dashboard?.porForma.length === 0 ? (
                <p className="mt-4 text-sm text-slate-400">Nenhum pagamento no período.</p>
              ) : (
                <div className="mt-4 space-y-4">
                  {dashboard?.porForma.map((forma) => (
                    <div key={forma.forma}>
                      <div className="mb-1 flex items-center justify-between text-xs">
                        <span className="text-slate-600">{forma.descricao}</span>
                        <span className="font-medium text-slate-800">{formatarMoeda(forma.total)}</span>
                      </div>
                      <div className="h-2 rounded-full bg-slate-100">
                        <div
                          className="h-2 rounded-full bg-brand-500"
                          style={{ width: `${Math.max(6, (forma.total / maiorForma) * 100)}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Cartao>

            <Cartao className="lg:col-span-2">
              <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
                <h2 className="text-sm font-semibold text-slate-800">Pagamentos do período</h2>
                <span className="text-xs text-slate-400">{pagamentos.length} registros</span>
              </div>
              {pagamentos.length === 0 ? (
                <Vazio titulo="Nenhum pagamento" descricao="Registre o primeiro pagamento do período." />
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
                      <tr>
                        <th className="px-3 py-2 font-medium">Data</th>
                        <th className="px-3 py-2 font-medium">Tutor</th>
                        <th className="px-3 py-2 font-medium">Forma</th>
                        <th className="px-3 py-2 font-medium">Status</th>
                        <th className="px-3 py-2 text-right font-medium">Valor</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {pagamentos.map((pagamento) => (
                        <tr key={pagamento.id}>
                          <td className="px-3 py-2 text-slate-500">{formatarData(pagamento.dataPagamento)}</td>
                          <td className="px-3 py-2">
                            <p className="font-medium text-slate-800">{pagamento.tutorNome}</p>
                            {pagamento.consultaDescricao && (
                              <p className="text-xs text-slate-400">{pagamento.consultaDescricao}</p>
                            )}
                          </td>
                          <td className="px-3 py-2 text-slate-600">{pagamento.formaPagamentoDescricao}</td>
                          <td className="px-3 py-2">
                            <Badge tom={pagamento.status === 'PAGO' ? 'verde' : 'ambar'}>
                              {pagamento.status}
                            </Badge>
                          </td>
                          <td className="px-3 py-2 text-right font-medium text-slate-800">
                            {formatarMoeda(pagamento.valor)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </Cartao>
          </div>
        </>
      )}

      <Modal
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        titulo="Registrar pagamento"
        descricao="Vincule o pagamento ao tutor e, se quiser, a uma consulta."
      >
        <form onSubmit={handleSubmit(salvar)} className="space-y-4" noValidate>
          <Campo label="Tutor" erro={errors.tutorId?.message} obrigatorio>
            <Selecao {...register('tutorId', { onChange: () => setValue('consultaId', '') })}>
              <option value="">Selecione o tutor</option>
              {tutores.map((tutor) => (
                <option key={tutor.id} value={tutor.id}>
                  {tutor.nome}
                </option>
              ))}
            </Selecao>
          </Campo>

          <Campo label="Consulta (opcional)" erro={errors.consultaId?.message}>
            <Selecao {...register('consultaId')} disabled={!tutorSelecionado}>
              <option value="">Sem consulta vinculada</option>
              {consultasDoTutor.map((consulta) => (
                <option key={consulta.id} value={consulta.id}>
                  {formatarData(consulta.dataHora)} · {consulta.animalNome}
                </option>
              ))}
            </Selecao>
          </Campo>

          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Valor (R$)" erro={errors.valor?.message} obrigatorio>
              <Entrada type="number" min="0.01" step="0.01" placeholder="180,00" {...register('valor')} />
            </Campo>
            <Campo label="Data do pagamento" erro={errors.dataPagamento?.message} obrigatorio>
              <Entrada type="date" max={hojeIso()} {...register('dataPagamento')} />
            </Campo>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Forma de pagamento" erro={errors.formaPagamento?.message} obrigatorio>
              <Selecao {...register('formaPagamento')}>
                {formas.map(([valor, rotulo]) => (
                  <option key={valor} value={valor}>
                    {rotulo}
                  </option>
                ))}
              </Selecao>
            </Campo>
            <Campo label="Status" erro={errors.status?.message} obrigatorio>
              <Selecao {...register('status')}>
                <option value="PAGO">Pago</option>
                <option value="PENDENTE">Pendente</option>
                <option value="CANCELADO">Cancelado</option>
              </Selecao>
            </Campo>
          </div>

          <Campo label="Observações" erro={errors.observacoes?.message}>
            <Entrada placeholder="Opcional" {...register('observacoes')} />
          </Campo>

          {erroApi && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {erroApi}
            </div>
          )}

          <div className="flex justify-end gap-2">
            <Botao type="button" variante="contorno" onClick={() => setModalAberto(false)}>
              Cancelar
            </Botao>
            <Botao type="submit" carregando={salvando}>
              Registrar pagamento
            </Botao>
          </div>
        </form>
      </Modal>
    </>
  )
}
