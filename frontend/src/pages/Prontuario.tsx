import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import {
  ArrowLeft,
  CalendarDays,
  ClipboardList,
  Copy,
  FileText,
  Pill,
  RefreshCw,
  Share2,
  ShieldCheck,
  Stethoscope,
  Syringe,
  Trash2,
  UserRound,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import { mensagemDeErro, prontuarioApi, vacinacoesApi } from '../lib/api'
import { formatarData, formatarDataHora, idade, hojeIso, statusProximaDose } from '../lib/format'
import type { CompartilhamentoProntuario, Prontuario } from '../types'
import {
  Badge,
  Botao,
  Campo,
  Carregando,
  Cartao,
  Confirmacao,
  Entrada,
  Modal,
  Vazio,
} from '../components/ui'

const schema = z.object({
  tipoVacina: z.string().min(2, 'Informe o tipo de vacina'),
  lote: z.string().optional(),
  dataAplicacao: z.string().min(1, 'Informe a data de aplicação'),
  proximaDose: z.string().optional(),
  observacoes: z.string().optional(),
})

type Formulario = z.infer<typeof schema>

const vazio: Formulario = {
  tipoVacina: '',
  lote: '',
  dataAplicacao: hojeIso(),
  proximaDose: '',
  observacoes: '',
}

export function ProntuarioAnimal({ compartilhado = false }: { compartilhado?: boolean }) {
  const { animalId, codigo } = useParams()
  const { usuario } = useAuth()
  const { mostrar } = useToast()
  const navigate = useNavigate()
  const id = Number(animalId)
  const podeExcluirVacina =
    !compartilhado && (usuario?.perfil === 'ADMINISTRADOR' || usuario?.perfil === 'VETERINARIO')
  const podeCompartilhar = !compartilhado && usuario?.perfil === 'VETERINARIO'

  const [dados, setDados] = useState<Prontuario | null>(null)
  const [aba, setAba] = useState<'atendimentos' | 'vacinas' | 'consultas'>('atendimentos')
  const [carregando, setCarregando] = useState(true)
  const [modalAberto, setModalAberto] = useState(false)
  const [excluindo, setExcluindo] = useState<{ id: number; tipo: string } | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [erroApi, setErroApi] = useState('')
  const [modalCompartilhar, setModalCompartilhar] = useState(false)
  const [compartilhamento, setCompartilhamento] = useState<CompartilhamentoProntuario | null>(null)
  const [gerandoCodigo, setGerandoCodigo] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Formulario>({ resolver: zodResolver(schema), defaultValues: vazio })

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = compartilhado
        ? await prontuarioApi.buscarPorCodigo(codigo ?? '')
        : await prontuarioApi.buscar(id)
      setDados(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [compartilhado, codigo, id, mostrar])

  useEffect(() => {
    carregar()
  }, [carregar])

  async function gerarCompartilhamento() {
    setGerandoCodigo(true)
    try {
      const { data } = await prontuarioApi.compartilhar(id)
      setCompartilhamento(data)
      setModalCompartilhar(true)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setGerandoCodigo(false)
    }
  }

  async function copiarCodigo() {
    if (!compartilhamento) return
    try {
      await navigator.clipboard.writeText(compartilhamento.codigo)
      mostrar('Código copiado.')
    } catch {
      mostrar('Não foi possível copiar automaticamente.', 'erro')
    }
  }

  async function revogarCompartilhamento() {
    try {
      await prontuarioApi.revogar(id)
      setCompartilhamento(null)
      setModalCompartilhar(false)
      mostrar('Compartilhamento revogado.')
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    }
  }

  async function salvarVacina(dadosForm: Formulario) {
    setSalvando(true)
    setErroApi('')
    try {
      await vacinacoesApi.criar(id, {
        tipoVacina: dadosForm.tipoVacina.trim(),
        lote: dadosForm.lote?.trim() || null,
        dataAplicacao: dadosForm.dataAplicacao,
        proximaDose: dadosForm.proximaDose || null,
        observacoes: dadosForm.observacoes?.trim() || null,
      })
      mostrar('Vacinação registrada.')
      setModalAberto(false)
      reset(vazio)
      await carregar()
      setAba('vacinas')
    } catch (error) {
      setErroApi(mensagemDeErro(error))
    } finally {
      setSalvando(false)
    }
  }

  async function confirmarExclusao() {
    if (!excluindo) return
    setSalvando(true)
    try {
      await vacinacoesApi.excluir(excluindo.id)
      mostrar('Vacinação excluída.')
      setExcluindo(null)
      await carregar()
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setSalvando(false)
    }
  }

  if (carregando) {
    return (
      <Cartao>
        <Carregando texto="Carregando prontuário..." />
      </Cartao>
    )
  }

  if (!dados) {
    return (
      <Cartao>
        <Vazio
          titulo="Prontuário não encontrado"
          descricao="O animal informado não existe ou foi removido."
          acao={<Botao onClick={() => navigate('/animais')}>Voltar para animais</Botao>}
        />
      </Cartao>
    )
  }

  const proxima = [...dados.vacinacoes]
    .filter((v) => v.proximaDose)
    .sort((a, b) => (a.proximaDose! > b.proximaDose! ? 1 : -1))[0]

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <Link
            to={compartilhado ? '/prontuarios' : usuario?.perfil === 'VETERINARIO' ? '/pacientes' : '/animais'}
            className="rounded-lg border border-slate-200 bg-white p-2 text-slate-500 hover:bg-slate-50"
            aria-label="Voltar"
          >
            <ArrowLeft className="h-4 w-4" />
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-semibold text-slate-900">{dados.animal.nome}</h1>
              <Badge tom="verde">{dados.animal.especie}</Badge>
            </div>
            <p className="text-sm text-slate-500">
              {dados.animal.raca || 'Raça não informada'} · {idade(dados.animal.dataNascimento)} ·{' '}
              Tutor(a): {dados.tutor.nome}
            </p>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          {podeCompartilhar && (
            <Botao variante="contorno" onClick={gerarCompartilhamento} carregando={gerandoCodigo}>
              <Share2 className="h-4 w-4" /> Compartilhar prontuário
            </Botao>
          )}
          {!compartilhado && (
            <Botao onClick={() => { setErroApi(''); reset(vazio); setModalAberto(true) }}>
              <Syringe className="h-4 w-4" /> Registrar vacina
            </Botao>
          )}
        </div>
      </div>

      {compartilhado && (
        <div className="flex items-center gap-2 rounded-xl border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-800">
          <ShieldCheck className="h-4 w-4 shrink-0" />
          Prontuário compartilhado pelo código <strong>{codigo}</strong>. Visualização somente leitura.
        </div>
      )}

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
            <ClipboardList className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Atendimentos</p>
            <p className="text-xl font-semibold text-slate-900">{dados.atendimentos.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 text-sky-700">
            <Syringe className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Vacinas</p>
            <p className="text-xl font-semibold text-slate-900">{dados.vacinacoes.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
            <CalendarDays className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Consultas</p>
            <p className="text-xl font-semibold text-slate-900">{dados.consultas.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-50 text-violet-700">
            <Pill className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Próxima dose</p>
            <p className="text-sm font-semibold text-slate-900">{statusProximaDose(proxima?.proximaDose).texto}</p>
          </div>
        </Cartao>
      </div>

      <Cartao>
        <div className="flex gap-1 border-b border-slate-100 px-4 pt-3">
          {(
            [
              ['atendimentos', `Atendimentos (${dados.atendimentos.length})`],
              ['vacinas', `Vacinas (${dados.vacinacoes.length})`],
              ['consultas', `Consultas (${dados.consultas.length})`],
            ] as const
          ).map(([chave, rotulo]) => (
            <button
              key={chave}
              onClick={() => setAba(chave)}
              className={`rounded-t-lg px-4 py-2.5 text-sm font-medium transition ${
                aba === chave
                  ? 'border-b-2 border-brand-600 text-brand-700'
                  : 'text-slate-500 hover:text-slate-800'
              }`}
            >
              {rotulo}
            </button>
          ))}
        </div>

        {aba === 'atendimentos' && (
          <div className="p-4">
            {dados.atendimentos.length === 0 ? (
              <Vazio
                titulo="Nenhum atendimento registrado"
                descricao="Os atendimentos aparecem aqui após o veterinário registrar a consulta."
              />
            ) : (
              <div className="space-y-4">
                {dados.atendimentos.map((atendimento) => (
                  <div key={atendimento.id} className="rounded-xl border border-slate-200 p-4">
                    <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-3">
                      <div className="flex items-center gap-2 text-sm font-medium text-slate-800">
                        <FileText className="h-4 w-4 text-brand-600" />
                        {formatarDataHora(atendimento.dataHora)}
                      </div>
                      <span className="flex items-center gap-1 text-xs text-slate-400">
                        <Stethoscope className="h-3.5 w-3.5" /> {atendimento.veterinarioNome}
                      </span>
                    </div>
                    <dl className="mt-3 grid gap-3 sm:grid-cols-2">
                      <Detalhe rotulo="Diagnóstico" valor={atendimento.diagnostico} destaque />
                      <Detalhe rotulo="Anamnese" valor={atendimento.anamnese} />
                      <Detalhe rotulo="Procedimentos" valor={atendimento.procedimentos} />
                      <Detalhe rotulo="Observações" valor={atendimento.observacoes} />
                    </dl>
                    {atendimento.prescricoes.length > 0 && (
                      <div className="mt-4">
                        <p className="mb-2 flex items-center gap-1.5 text-xs font-semibold tracking-wide text-slate-500 uppercase">
                          <Pill className="h-3.5 w-3.5" /> Prescrições
                        </p>
                        <div className="overflow-x-auto rounded-lg border border-slate-200">
                          <table className="w-full text-left text-sm">
                            <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
                              <tr>
                                <th className="px-3 py-2 font-medium">Medicamento</th>
                                <th className="px-3 py-2 font-medium">Dosagem</th>
                                <th className="px-3 py-2 font-medium">Frequência</th>
                                <th className="px-3 py-2 font-medium">Duração</th>
                                <th className="px-3 py-2 font-medium">Orientações</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                              {atendimento.prescricoes.map((prescricao) => (
                                <tr key={prescricao.id}>
                                  <td className="px-3 py-2 font-medium text-slate-800">{prescricao.medicamento}</td>
                                  <td className="px-3 py-2 text-slate-600">{prescricao.dosagem || '—'}</td>
                                  <td className="px-3 py-2 text-slate-600">{prescricao.frequencia || '—'}</td>
                                  <td className="px-3 py-2 text-slate-600">{prescricao.duracao || '—'}</td>
                                  <td className="px-3 py-2 text-slate-500">{prescricao.orientacoes || '—'}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {aba === 'vacinas' && (
          <div className="p-4">
            {dados.vacinacoes.length === 0 ? (
              <Vazio
                titulo="Nenhuma vacina registrada"
                descricao="Registre a primeira vacina para acompanhar a próxima dose."
                acao={!compartilhado ? <Botao onClick={() => setModalAberto(true)}>Registrar vacina</Botao> : undefined}
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
                    <tr>
                      <th className="px-3 py-2 font-medium">Vacina</th>
                      <th className="px-3 py-2 font-medium">Aplicação</th>
                      <th className="px-3 py-2 font-medium">Próxima dose</th>
                      <th className="px-3 py-2 font-medium">Aplicada por</th>
                      <th className="px-3 py-2 font-medium">Lote</th>
                      {podeExcluirVacina && <th className="px-3 py-2 text-right font-medium">Ações</th>}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {dados.vacinacoes.map((vacinacao) => {
                      const status = statusProximaDose(vacinacao.proximaDose)
                      return (
                        <tr key={vacinacao.id}>
                          <td className="px-3 py-2 font-medium text-slate-800">{vacinacao.tipoVacina}</td>
                          <td className="px-3 py-2 text-slate-600">{formatarData(vacinacao.dataAplicacao)}</td>
                          <td className="px-3 py-2">
                            <Badge tom={status.tom}>{status.texto}</Badge>
                          </td>
                          <td className="px-3 py-2 text-slate-600">{vacinacao.veterinarioNome || 'Recepção'}</td>
                          <td className="px-3 py-2 text-slate-500">{vacinacao.lote || '—'}</td>
                          {podeExcluirVacina && (
                            <td className="px-3 py-2 text-right">
                              <button
                                onClick={() => setExcluindo({ id: vacinacao.id, tipo: vacinacao.tipoVacina })}
                                className="rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-600"
                                aria-label={`Excluir vacina ${vacinacao.tipoVacina}`}
                              >
                                <Trash2 className="h-4 w-4" />
                              </button>
                            </td>
                          )}
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {aba === 'consultas' && (
          <div className="p-4">
            {dados.consultas.length === 0 ? (
              <Vazio titulo="Nenhuma consulta" descricao="Ainda não há consultas para este animal." />
            ) : (
              <ul className="divide-y divide-slate-100">
                {dados.consultas.map((consulta) => (
                  <li key={consulta.id} className="flex flex-wrap items-center gap-3 py-3">
                    <span className="w-16 rounded-lg bg-brand-50 px-2 py-1 text-center text-xs font-semibold text-brand-700">
                      {formatarDataHora(consulta.dataHora)}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="flex items-center gap-1.5 text-sm text-slate-700">
                        <Stethoscope className="h-3.5 w-3.5 text-slate-400" /> {consulta.veterinarioNome}
                      </p>
                      <p className="flex items-center gap-1.5 text-xs text-slate-400">
                        <UserRound className="h-3 w-3" /> {consulta.tutorNome}
                      </p>
                    </div>
                    <Badge
                      tom={
                        consulta.status === 'AGENDADA'
                          ? 'verde'
                          : consulta.status === 'CANCELADA'
                            ? 'vermelho'
                            : 'azul'
                      }
                    >
                      {consulta.status}
                    </Badge>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </Cartao>

      <Modal
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        titulo="Registrar vacina"
        descricao={`${dados.animal.nome} · ${dados.tutor.nome}`}
      >
        <form onSubmit={handleSubmit(salvarVacina)} className="space-y-4" noValidate>
          <Campo label="Tipo de vacina" erro={errors.tipoVacina?.message} obrigatorio>
            <Entrada placeholder="V10 - Polivalente" {...register('tipoVacina')} />
          </Campo>
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Data de aplicação" erro={errors.dataAplicacao?.message} obrigatorio>
              <Entrada type="date" max={hojeIso()} {...register('dataAplicacao')} />
            </Campo>
            <Campo label="Próxima dose" erro={errors.proximaDose?.message}>
              <Entrada type="date" {...register('proximaDose')} />
            </Campo>
          </div>
          <Campo label="Lote" erro={errors.lote?.message}>
            <Entrada placeholder="LOTE-2026-A" {...register('lote')} />
          </Campo>
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
              Registrar vacina
            </Botao>
          </div>
        </form>
      </Modal>

      <Confirmacao
        aberto={!!excluindo}
        titulo="Excluir vacinação"
        mensagem={`Deseja excluir o registro da vacina ${excluindo?.tipo ?? ''}?`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
        carregando={salvando}
      />

      <Modal
        aberto={modalCompartilhar}
        onFechar={() => setModalCompartilhar(false)}
        titulo="Compartilhar prontuário"
        descricao={`${dados.animal.nome} · ${dados.tutor.nome}`}
        largura="max-w-md"
      >
        {compartilhamento ? (
          <div className="space-y-4">
            <p className="text-sm text-slate-600">
              Envie este código para outro veterinário. Ele poderá consultar o prontuário em modo
              somente leitura.
            </p>
            <div className="rounded-xl border border-dashed border-brand-300 bg-brand-50 px-4 py-6 text-center">
              <p className="text-xs tracking-widest text-brand-600 uppercase">Código do prontuário</p>
              <p className="mt-2 font-mono text-3xl font-bold tracking-[0.3em] text-brand-800">
                {compartilhamento.codigo}
              </p>
              <p className="mt-2 text-xs text-slate-500">
                Válido até {formatarDataHora(compartilhamento.expiraEm)}
              </p>
            </div>
            <div className="flex flex-wrap justify-between gap-2">
              <Botao variante="contorno" onClick={copiarCodigo}>
                <Copy className="h-4 w-4" /> Copiar código
              </Botao>
              <Botao variante="secundario" onClick={gerarCompartilhamento} carregando={gerandoCodigo}>
                <RefreshCw className="h-4 w-4" /> Gerar novo código
              </Botao>
            </div>
            <div className="flex justify-between border-t border-slate-100 pt-4">
              <Botao variante="perigo" onClick={revogarCompartilhamento}>
                Revogar acesso
              </Botao>
              <Botao variante="contorno" onClick={() => setModalCompartilhar(false)}>
                Fechar
              </Botao>
            </div>
          </div>
        ) : (
          <p className="text-sm text-slate-500">Gerando código...</p>
        )}
      </Modal>
    </div>
  )
}

function Detalhe({
  rotulo,
  valor,
  destaque,
}: {
  rotulo: string
  valor?: string | null
  destaque?: boolean
}) {
  return (
    <div>
      <dt className="text-xs font-medium tracking-wide text-slate-400 uppercase">{rotulo}</dt>
      <dd className={destaque ? 'mt-1 text-sm font-semibold text-slate-900' : 'mt-1 text-sm text-slate-600'}>
        {valor || '—'}
      </dd>
    </div>
  )
}
