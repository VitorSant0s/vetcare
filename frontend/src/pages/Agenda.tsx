import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import {
  Ban,
  CalendarOff,
  CalendarDays,
  CalendarPlus,
  ChevronLeft,
  ChevronRight,
  ClipboardPlus,
  Clock,
  FileText,
  PawPrint,
  Stethoscope,
  UserRound,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import {
  animaisApi,
  bloqueiosApi,
  camposDeErro,
  consultasApi,
  mensagemDeErro,
  tutoresApi,
  veterinariosApi,
} from '../lib/api'
import { formatarData, formatarDataHora, formatarHora, hojeIso, paraInputDateTime, somarDiasIso } from '../lib/format'
import type { Animal, BloqueioAgenda, Consulta, Tutor, Usuario } from '../types'
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
import { AtendimentoModal } from '../components/AtendimentoModal'

const schema = z.object({
  tutorId: z.string().min(1, 'Selecione o tutor'),
  animalId: z.string().min(1, 'Selecione o animal'),
  veterinarioId: z.string().min(1, 'Selecione o veterinário'),
  dataHora: z.string().min(1, 'Informe a data e o horário'),
  observacoes: z.string().optional(),
})

type Formulario = z.infer<typeof schema>

function amanhaAs9(): string {
  const data = new Date()
  data.setDate(data.getDate() + 1)
  data.setHours(9, 0, 0, 0)
  return paraInputDateTime(data)
}

export function Agenda() {
  const { usuario } = useAuth()
  const { mostrar } = useToast()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const podeAgendar = usuario?.perfil !== 'VETERINARIO'

  const [consultas, setConsultas] = useState<Consulta[]>([])
  const [dataBase, setDataBase] = useState(hojeIso())
  const [visao, setVisao] = useState<'DIA' | 'SEMANA'>('SEMANA')
  const inicio = dataBase
  const fim = visao === 'DIA' ? dataBase : somarDiasIso(dataBase, 6)
  const [veterinarioFiltro, setVeterinarioFiltro] = useState('')
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([])
  const [tutores, setTutores] = useState<Tutor[]>([])
  const [animais, setAnimais] = useState<Animal[]>([])
  const [carregando, setCarregando] = useState(true)
  const [modalAberto, setModalAberto] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [erroApi, setErroApi] = useState('')
  const [cancelando, setCancelando] = useState<Consulta | null>(null)
  const [motivoCancelamento, setMotivoCancelamento] = useState('')
  const [processandoCancelamento, setProcessandoCancelamento] = useState(false)
  const [atendendo, setAtendendo] = useState<Consulta | null>(null)
  const [bloqueios, setBloqueios] = useState<BloqueioAgenda[]>([])
  const [modalBloqueio, setModalBloqueio] = useState(false)
  const [dataBloqueio, setDataBloqueio] = useState(somarDiasIso(hojeIso(), 1))
  const [motivoBloqueio, setMotivoBloqueio] = useState('')
  const [erroBloqueio, setErroBloqueio] = useState('')
  const [salvandoBloqueio, setSalvandoBloqueio] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    setError,
    formState: { errors },
  } = useForm<Formulario>({
    resolver: zodResolver(schema),
    defaultValues: {
      tutorId: '',
      animalId: '',
      veterinarioId: '',
      dataHora: amanhaAs9(),
      observacoes: '',
    },
  })

  const tutorSelecionado = watch('tutorId')

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await consultasApi.listar(
        inicio,
        fim,
        veterinarioFiltro ? Number(veterinarioFiltro) : undefined,
      )
      setConsultas(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [inicio, fim, veterinarioFiltro, mostrar])

  useEffect(() => {
    carregar()
  }, [carregar])

  const carregarBloqueios = useCallback(async () => {
    if (usuario?.perfil !== 'VETERINARIO') return
    try {
      const { data } = await bloqueiosApi.listar(inicio, fim)
      setBloqueios(data)
    } catch {
      setBloqueios([])
    }
  }, [usuario?.perfil, inicio, fim])

  useEffect(() => {
    carregarBloqueios()
  }, [carregarBloqueios])

  useEffect(() => {
    veterinariosApi
      .listar()
      .then(({ data }) => setVeterinarios(data))
      .catch(() => setVeterinarios([]))
  }, [])

  useEffect(() => {
    if (!tutorSelecionado) {
      setAnimais([])
      return
    }
    let ativo = true
    animaisApi
      .porTutor(Number(tutorSelecionado))
      .then(({ data }) => {
        if (ativo) setAnimais(data)
      })
      .catch(() => {
        if (ativo) setAnimais([])
      })
    return () => {
      ativo = false
    }
  }, [tutorSelecionado])

  const grupos = useMemo(() => {
    const mapa = new Map<string, Consulta[]>()
    for (const consulta of consultas) {
      const dia = consulta.dataHora.slice(0, 10)
      const lista = mapa.get(dia) ?? []
      lista.push(consulta)
      mapa.set(dia, lista)
    }
    return [...mapa.entries()]
  }, [consultas])

  const agora = new Date()
  const hoje = hojeIso()
  const proximas = consultas.filter((c) => new Date(c.dataHora) >= agora).length

  async function abrirModal() {
    setErroApi('')
    reset({
      tutorId: '',
      animalId: '',
      veterinarioId: veterinarios[0] ? String(veterinarios[0].id) : '',
      dataHora: amanhaAs9(),
      observacoes: '',
    })
    setAnimais([])
    try {
      const { data } = await tutoresApi.listar(undefined, 0, 100)
      setTutores(data.content)
    } catch {
      setTutores([])
    }
    setModalAberto(true)
  }

  useEffect(() => {
    if (searchParams.get('nova') === '1' && podeAgendar) {
      abrirModal()
      const proximos = new URLSearchParams(searchParams)
      proximos.delete('nova')
      setSearchParams(proximos, { replace: true })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, podeAgendar])

  async function salvar(dados: Formulario) {
    setSalvando(true)
    setErroApi('')
    try {
      await consultasApi.criar({
        tutorId: Number(dados.tutorId),
        animalId: Number(dados.animalId),
        veterinarioId: Number(dados.veterinarioId),
        dataHora: dados.dataHora.length === 16 ? `${dados.dataHora}:00` : dados.dataHora,
        observacoes: dados.observacoes?.trim() || null,
      })
      mostrar('Consulta agendada com sucesso.')
      setModalAberto(false)
      await carregar()
    } catch (error) {
      setErroApi(mensagemDeErro(error))
      Object.entries(camposDeErro(error)).forEach(([campo, mensagem]) => {
        if (campo in schema.shape) {
          setError(campo as keyof Formulario, { message: mensagem })
        }
      })
    } finally {
      setSalvando(false)
    }
  }

  function podeAtender(consulta: Consulta): boolean {
    const perfilPermitido =
      usuario?.perfil === 'VETERINARIO' || usuario?.perfil === 'ADMINISTRADOR'
    return (
      perfilPermitido &&
      consulta.status === 'AGENDADA' &&
      new Date(consulta.dataHora) <= new Date(Date.now() + 30 * 60000)
    )
  }

  async function bloquearDia() {
    setErroBloqueio('')
    if (!motivoBloqueio.trim()) {
      setErroBloqueio('Informe o motivo do bloqueio.')
      return
    }
    setSalvandoBloqueio(true)
    try {
      const { data } = await bloqueiosApi.criar(dataBloqueio, motivoBloqueio.trim())
      mostrar(
        `Dia ${formatarData(dataBloqueio)} bloqueado. ${data.consultasCanceladas} consulta(s) cancelada(s).`,
      )
      setModalBloqueio(false)
      setMotivoBloqueio('')
      await Promise.all([carregar(), carregarBloqueios()])
    } catch (error) {
      setErroBloqueio(mensagemDeErro(error))
    } finally {
      setSalvandoBloqueio(false)
    }
  }

  async function desbloquearDia(data: string) {
    try {
      await bloqueiosApi.excluir(data)
      mostrar(`Dia ${formatarData(data)} desbloqueado.`)
      await carregarBloqueios()
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    }
  }

  async function confirmarCancelamento() {
    if (!cancelando) return
    setProcessandoCancelamento(true)
    try {
      await consultasApi.cancelar(cancelando.id, motivoCancelamento)
      mostrar('Consulta cancelada e horário liberado.')
      setCancelando(null)
      setMotivoCancelamento('')
      await carregar()
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setProcessandoCancelamento(false)
    }
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Agenda"
        descricao={
          usuario?.perfil === 'VETERINARIO'
            ? 'Suas consultas agendadas no período.'
            : 'Consultas da clínica e agendamento de novos atendimentos.'
        }
        acao={
          podeAgendar && (
            <Botao onClick={abrirModal}>
              <CalendarPlus className="h-4 w-4" /> Nova consulta
            </Botao>
          )
        }
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
            <CalendarDays className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">No período</p>
            <p className="text-xl font-semibold text-slate-900">{consultas.length}</p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 text-sky-700">
            <Clock className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Hoje</p>
            <p className="text-xl font-semibold text-slate-900">
              {consultas.filter((c) => c.dataHora.slice(0, 10) === hoje).length}
            </p>
          </div>
        </Cartao>
        <Cartao className="flex items-center gap-3 p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
            <Stethoscope className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs text-slate-500">Próximas</p>
            <p className="text-xl font-semibold text-slate-900">{proximas}</p>
          </div>
        </Cartao>
      </div>

      <Cartao className="mb-6 p-4">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-end">
          <div className="flex items-end gap-2">
            <button
              type="button"
              onClick={() => setDataBase(somarDiasIso(dataBase, visao === 'DIA' ? -1 : -7))}
              className="mb-0.5 rounded-lg border border-slate-300 p-2 text-slate-500 hover:bg-slate-50"
              aria-label="Período anterior"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <Campo label="Data de referência">
              <Entrada type="date" value={dataBase} onChange={(e) => setDataBase(e.target.value)} />
            </Campo>
            <button
              type="button"
              onClick={() => setDataBase(somarDiasIso(dataBase, visao === 'DIA' ? 1 : 7))}
              className="mb-0.5 rounded-lg border border-slate-300 p-2 text-slate-500 hover:bg-slate-50"
              aria-label="Próximo período"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
            <Botao variante="contorno" onClick={() => setDataBase(hojeIso())}>
              Hoje
            </Botao>
          </div>

          <div className="mb-0.5 flex rounded-lg border border-slate-300 bg-white p-0.5">
            {(['DIA', 'SEMANA'] as const).map((modo) => (
              <button
                key={modo}
                type="button"
                onClick={() => setVisao(modo)}
                className={`rounded-md px-3 py-1.5 text-sm font-medium transition ${
                  visao === modo ? 'bg-brand-600 text-white' : 'text-slate-500 hover:text-slate-800'
                }`}
              >
                {modo === 'DIA' ? 'Dia' : 'Semana'}
              </button>
            ))}
          </div>

          {usuario?.perfil !== 'VETERINARIO' && (
            <div className="w-full sm:w-64">
              <Campo label="Veterinário">
                <Selecao value={veterinarioFiltro} onChange={(e) => setVeterinarioFiltro(e.target.value)}>
                  <option value="">Todos</option>
                  {veterinarios.map((vet) => (
                    <option key={vet.id} value={vet.id}>
                      {vet.nome}
                    </option>
                  ))}
                </Selecao>
              </Campo>
            </div>
          )}

          <p className="text-xs text-slate-400 lg:ml-auto">
            {visao === 'DIA'
              ? `Consultas de ${dataBase.split('-').reverse().join('/')}`
              : `Semana de ${dataBase.split('-').reverse().join('/')} a ${fim.split('-').reverse().join('/')}`}
          </p>
        </div>
      </Cartao>

      {usuario?.perfil === 'VETERINARIO' && (
        <Cartao className="mb-6 p-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-800">
                <CalendarOff className="h-4 w-4 text-amber-600" /> Bloqueios de agenda
              </h2>
              <p className="mt-0.5 text-xs text-slate-500">
                Ao bloquear um dia, as consultas agendadas nele são canceladas e os tutores são notificados.
              </p>
            </div>
            <Botao variante="contorno" onClick={() => { setErroBloqueio(''); setModalBloqueio(true) }}>
              <CalendarOff className="h-4 w-4" /> Bloquear dia
            </Botao>
          </div>
          {bloqueios.length > 0 && (
            <ul className="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-200">
              {bloqueios.map((bloqueio) => (
                <li key={bloqueio.id} className="flex flex-col gap-2 px-3 py-2 sm:flex-row sm:items-center">
                  <span className="rounded-lg bg-amber-50 px-2 py-1 text-xs font-semibold text-amber-700">
                    {formatarData(bloqueio.data)}
                  </span>
                  <p className="min-w-0 flex-1 text-sm text-slate-600">{bloqueio.motivo}</p>
                  <button
                    onClick={() => desbloquearDia(bloqueio.data)}
                    className="text-xs font-medium text-brand-700 hover:underline"
                  >
                    Desbloquear
                  </button>
                </li>
              ))}
            </ul>
          )}
        </Cartao>
      )}

      {carregando ? (
        <Cartao>
          <Carregando />
        </Cartao>
      ) : consultas.length === 0 ? (
        <Cartao>
          <Vazio
            titulo="Nenhuma consulta no período"
            descricao="Ajuste as datas ou agende uma nova consulta."
            acao={podeAgendar ? <Botao onClick={abrirModal}>Agendar consulta</Botao> : undefined}
          />
        </Cartao>
      ) : (
        <div className="space-y-6">
          {grupos.map(([dia, itens]) => (
            <section key={dia}>
              <div className="mb-2 flex items-center gap-2">
                <h2 className="text-sm font-semibold text-slate-700 capitalize">
                  {new Date(`${dia}T12:00:00`).toLocaleDateString('pt-BR', {
                    weekday: 'long',
                    day: '2-digit',
                    month: 'long',
                  })}
                </h2>
                <span className="text-xs text-slate-400">
                  {itens.length} {itens.length === 1 ? 'consulta' : 'consultas'}
                </span>
              </div>
              <div className="space-y-2">
                {itens.map((consulta) => (
                  <Cartao key={consulta.id} className="p-4">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                      <div className="flex w-20 shrink-0 items-center justify-center rounded-lg bg-brand-50 px-3 py-2 text-brand-700">
                        <Clock className="mr-1.5 h-4 w-4" />
                        <span className="text-sm font-semibold">{formatarHora(consulta.dataHora)}</span>
                      </div>
                      <div className="grid flex-1 gap-2 sm:grid-cols-3">
                        <div>
                          <p className="flex items-center gap-1.5 text-xs text-slate-400">
                            <PawPrint className="h-3.5 w-3.5" /> Animal
                          </p>
                          <p className="text-sm font-medium text-slate-800">{consulta.animalNome}</p>
                        </div>
                        <div>
                          <p className="flex items-center gap-1.5 text-xs text-slate-400">
                            <UserRound className="h-3.5 w-3.5" /> Tutor
                          </p>
                          <p className="text-sm text-slate-700">{consulta.tutorNome}</p>
                        </div>
                        <div>
                          <p className="flex items-center gap-1.5 text-xs text-slate-400">
                            <Stethoscope className="h-3.5 w-3.5" /> Veterinário
                          </p>
                          <p className="text-sm text-slate-700">{consulta.veterinarioNome}</p>
                        </div>
                      </div>
                      <div className="flex items-center justify-between gap-3 sm:flex-col sm:items-end">
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
                        {consulta.motivoCancelamento && (
                          <p className="max-w-52 text-right text-xs text-red-400 sm:text-right">
                            Motivo: {consulta.motivoCancelamento}
                          </p>
                        )}
                        {consulta.observacoes && (
                          <p className="max-w-52 text-right text-xs text-slate-400 sm:text-right">
                            {consulta.observacoes}
                          </p>
                        )}
                        <div className="flex flex-wrap items-center justify-end gap-2">
                          <Link
                            to={`/animais/${consulta.animalId}/prontuario`}
                            className="inline-flex items-center gap-1 text-xs font-medium text-brand-700 hover:underline"
                          >
                            <FileText className="h-3.5 w-3.5" /> Prontuário
                          </Link>
                          {podeAtender(consulta) && (
                            <button
                              type="button"
                              onClick={() => setAtendendo(consulta)}
                              className="inline-flex items-center gap-1 text-xs font-medium text-sky-700 hover:underline"
                            >
                              <ClipboardPlus className="h-3.5 w-3.5" /> Atender
                            </button>
                          )}
                          {(usuario?.perfil !== 'VETERINARIO' ||
                            consulta.veterinarioId === usuario?.id) &&
                            consulta.status === 'AGENDADA' &&
                            new Date(consulta.dataHora) > agora && (
                              <button
                                type="button"
                                onClick={() => {
                                  setCancelando(consulta)
                                  setMotivoCancelamento('')
                                }}
                                className="inline-flex items-center gap-1 text-xs font-medium text-red-600 hover:underline"
                              >
                                <Ban className="h-3.5 w-3.5" /> Cancelar
                              </button>
                            )}
                        </div>
                      </div>
                    </div>
                  </Cartao>
                ))}
              </div>
            </section>
          ))}
        </div>
      )}

      <Modal
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        titulo="Nova consulta"
        descricao="Relacione animal, tutor, veterinário, data e horário."
      >
        <form onSubmit={handleSubmit(salvar)} className="space-y-4" noValidate>
          <Campo label="Tutor" erro={errors.tutorId?.message} obrigatorio>
            <Selecao
              {...register('tutorId', {
                onChange: () => setValue('animalId', ''),
              })}
            >
              <option value="">Selecione o tutor</option>
              {tutores.map((tutor) => (
                <option key={tutor.id} value={tutor.id}>
                  {tutor.nome} · {tutor.cpf}
                </option>
              ))}
            </Selecao>
          </Campo>

          <Campo
            label="Animal"
            erro={errors.animalId?.message}
            obrigatorio
          >
            <Selecao {...register('animalId')} disabled={!tutorSelecionado}>
              <option value="">
                {tutorSelecionado
                  ? animais.length === 0
                    ? 'Nenhum animal para este tutor'
                    : 'Selecione o animal'
                  : 'Selecione primeiro o tutor'}
              </option>
              {animais.map((animal) => (
                <option key={animal.id} value={animal.id}>
                  {animal.nome} · {animal.especie}
                </option>
              ))}
            </Selecao>
          </Campo>

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
            <Campo label="Data e horário" erro={errors.dataHora?.message} obrigatorio>
              <Entrada type="datetime-local" min={paraInputDateTime(new Date())} {...register('dataHora')} />
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

          <div className="flex justify-end gap-2 pt-2">
            <Botao type="button" variante="contorno" onClick={() => setModalAberto(false)}>
              Cancelar
            </Botao>
            <Botao type="submit" carregando={salvando}>
              Agendar consulta
            </Botao>
          </div>
        </form>
      </Modal>

      <Modal
        aberto={!!cancelando}
        onFechar={() => setCancelando(null)}
        titulo="Cancelar consulta"
        descricao={
          cancelando
            ? `${cancelando.animalNome} · ${formatarDataHora(cancelando.dataHora)} · ${cancelando.veterinarioNome}`
            : undefined
        }
        largura="max-w-md"
      >
        <div className="space-y-4">
          <Campo label="Motivo do cancelamento (opcional)">
            <Entrada
              value={motivoCancelamento}
              onChange={(e) => setMotivoCancelamento(e.target.value)}
              placeholder="Ex.: tutor pediu para remarcar"
              maxLength={300}
            />
          </Campo>
          <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
            O horário ficará livre para um novo agendamento e a ação será registrada na auditoria.
          </p>
          <div className="flex justify-end gap-2">
            <Botao variante="contorno" onClick={() => setCancelando(null)}>
              Voltar
            </Botao>
            <Botao
              variante="perigo"
              onClick={confirmarCancelamento}
              carregando={processandoCancelamento}
            >
              Confirmar cancelamento
            </Botao>
          </div>
        </div>
      </Modal>

      <Modal
        aberto={modalBloqueio}
        onFechar={() => setModalBloqueio(false)}
        titulo="Bloquear dia de trabalho"
        descricao="As consultas agendadas nesse dia serão canceladas com o motivo informado."
        largura="max-w-md"
      >
        <div className="space-y-4">
          <Campo label="Data" obrigatorio>
            <Entrada
              type="date"
              min={hojeIso()}
              value={dataBloqueio}
              onChange={(e) => setDataBloqueio(e.target.value)}
            />
          </Campo>
          <Campo label="Motivo" obrigatorio>
            <Entrada
              value={motivoBloqueio}
              onChange={(e) => setMotivoBloqueio(e.target.value)}
              placeholder="Ex.: congresso, atestado, férias"
              maxLength={500}
            />
          </Campo>
          <div className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            As consultas do dia serão canceladas, liberadas da agenda e os tutores entrarão na fila de
            notificações.
          </div>
          {erroBloqueio && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {erroBloqueio}
            </div>
          )}
          <div className="flex justify-end gap-2">
            <Botao variante="contorno" onClick={() => setModalBloqueio(false)}>
              Cancelar
            </Botao>
            <Botao variante="perigo" onClick={bloquearDia} carregando={salvandoBloqueio}>
              Bloquear dia
            </Botao>
          </div>
        </div>
      </Modal>

      <AtendimentoModal
        consulta={atendendo}
        onFechar={() => setAtendendo(null)}
        onSucesso={(animalId) => {
          setAtendendo(null)
          mostrar('Atendimento registrado e consulta concluída.')
          navigate(`/animais/${animalId}/prontuario`)
        }}
      />
    </>
  )
}
