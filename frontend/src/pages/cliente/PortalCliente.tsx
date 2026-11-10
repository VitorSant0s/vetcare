import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import {
  CalendarPlus,
  ClipboardList,
  Clock,
  KeyRound,
  PawPrint,
  Pencil,
  Plus,
} from 'lucide-react'
import { useClienteAuth } from '../../context/ClienteAuthContext'
import { useToast } from '../../components/Toast'
import { clienteApi, mensagemDeErro } from '../../lib/api'
import { formatarDataHora, idade, paraInputDateTime } from '../../lib/format'
import type { Animal, SolicitacaoAgendamento } from '../../types'
import {
  Badge,
  Botao,
  Campo,
  Carregando,
  Cartao,
  Entrada,
  Modal,
  Selecao,
  Vazio,
} from '../../components/ui'

const petSchema = z.object({
  nome: z.string().min(2, 'Informe o nome do pet'),
  especie: z.string().min(1, 'Selecione a espécie'),
  raca: z.string().optional(),
  sexo: z.string().optional(),
  dataNascimento: z.string().optional(),
})

type PetForm = z.infer<typeof petSchema>

const petVazio: PetForm = { nome: '', especie: 'Cão', raca: '', sexo: '', dataNascimento: '' }

const senhaSchema = z
  .object({
    atual: z.string().min(1, 'Informe a senha atual'),
    nova: z.string().min(6, 'A nova senha deve ter ao menos 6 caracteres'),
    confirmacao: z.string().min(1, 'Confirme a nova senha'),
  })
  .refine((dados) => dados.nova === dados.confirmacao, {
    path: ['confirmacao'],
    message: 'As senhas não coincidem',
  })

type SenhaForm = z.infer<typeof senhaSchema>

export function PortalCliente() {
  const { tutor, alterarSenha, marcarSenhaAlterada } = useClienteAuth()
  const primeiroAcesso = tutor?.primeiroAcesso ?? false
  const { mostrar } = useToast()
  const [params, setParams] = useSearchParams()
  const aba = params.get('aba') ?? 'inicio'

  const [pets, setPets] = useState<Animal[]>([])
  const [solicitacoes, setSolicitacoes] = useState<SolicitacaoAgendamento[]>([])
  const [carregando, setCarregando] = useState(true)
  const [modalPet, setModalPet] = useState(false)
  const [petEditando, setPetEditando] = useState<Animal | null>(null)
  const [salvandoPet, setSalvandoPet] = useState(false)
  const [erroPet, setErroPet] = useState('')
  const [modalSenha, setModalSenha] = useState(false)
  const [salvandoSenha, setSalvandoSenha] = useState(false)
  const [erroSenha, setErroSenha] = useState('')

  const [petSelecionado, setPetSelecionado] = useState('')
  const [dataPreferida, setDataPreferida] = useState(() => {
    const data = new Date()
    data.setDate(data.getDate() + 1)
    data.setHours(9, 0, 0, 0)
    return paraInputDateTime(data)
  })
  const [motivo, setMotivo] = useState('')
  const [erroPedido, setErroPedido] = useState('')
  const [enviandoPedido, setEnviandoPedido] = useState(false)

  const petForm = useForm<PetForm>({ resolver: zodResolver(petSchema), defaultValues: petVazio })
  const senhaForm = useForm<SenhaForm>({ resolver: zodResolver(senhaSchema) })

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const [petsResp, pedidosResp] = await Promise.all([
        clienteApi.listarPets(),
        clienteApi.listarSolicitacoes(),
      ])
      setPets(petsResp.data)
      setSolicitacoes(pedidosResp.data)
      if (!petSelecionado && petsResp.data.length > 0) setPetSelecionado(String(petsResp.data[0].id))
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [mostrar, petSelecionado])

  useEffect(() => {
    carregar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (primeiroAcesso) setModalSenha(true)
  }, [primeiroAcesso])

  const pendentes = solicitacoes.filter((s) => s.status === 'PENDENTE').length
  const aprovadas = solicitacoes.filter((s) => s.status === 'APROVADA').length
  const proxima = useMemo(
    () =>
      [...solicitacoes]
        .filter((s) => s.status === 'APROVADA')
        .sort((a, b) => (a.dataPreferida > b.dataPreferida ? 1 : -1))[0],
    [solicitacoes],
  )

  function irPara(novaAba: string) {
    setParams({ aba: novaAba })
  }

  function abrirNovoPet() {
    setPetEditando(null)
    setErroPet('')
    petForm.reset(petVazio)
    setModalPet(true)
  }

  function abrirEdicaoPet(pet: Animal) {
    setPetEditando(pet)
    setErroPet('')
    petForm.reset({
      nome: pet.nome,
      especie: pet.especie,
      raca: pet.raca ?? '',
      sexo: pet.sexo ?? '',
      dataNascimento: pet.dataNascimento ?? '',
    })
    setModalPet(true)
  }

  async function salvarPet(dados: PetForm) {
    setSalvandoPet(true)
    setErroPet('')
    const payload = {
      nome: dados.nome.trim(),
      especie: dados.especie,
      raca: dados.raca?.trim() || null,
      sexo: dados.sexo || null,
      dataNascimento: dados.dataNascimento || null,
    }
    try {
      if (petEditando) {
        await clienteApi.atualizarPet(petEditando.id, payload)
        mostrar('Dados do pet atualizados.')
      } else {
        await clienteApi.criarPet(payload)
        mostrar('Pet cadastrado com sucesso.')
      }
      setModalPet(false)
      await carregar()
    } catch (error) {
      setErroPet(mensagemDeErro(error))
    } finally {
      setSalvandoPet(false)
    }
  }

  async function salvarSenha(dados: SenhaForm) {
    setSalvandoSenha(true)
    setErroSenha('')
    try {
      await alterarSenha(dados.atual, dados.nova)
      marcarSenhaAlterada()
      mostrar('Senha alterada com sucesso.')
      setModalSenha(false)
      senhaForm.reset()
    } catch (error) {
      setErroSenha(mensagemDeErro(error))
    } finally {
      setSalvandoSenha(false)
    }
  }

  async function pedirAgendamento() {
    setErroPedido('')
    if (!petSelecionado) {
      setErroPedido('Selecione o pet.')
      return
    }
    if (!dataPreferida) {
      setErroPedido('Informe a data e o horário preferidos.')
      return
    }
    setEnviandoPedido(true)
    try {
      await clienteApi.solicitar({
        animalId: Number(petSelecionado),
        dataPreferida: dataPreferida.length === 16 ? `${dataPreferida}:00` : dataPreferida,
        motivo: motivo.trim() || null,
      })
      mostrar('Pedido enviado. A recepção confirmará o horário.')
      setMotivo('')
      await carregar()
      irPara('pedidos')
    } catch (error) {
      setErroPedido(mensagemDeErro(error))
    } finally {
      setEnviandoPedido(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-xs font-medium tracking-widest text-brand-600 uppercase">
            Portal do cliente
          </p>
          <h1 className="text-2xl font-semibold text-slate-900">Olá, {tutor?.nome.split(' ')[0]}!</h1>
          <p className="text-sm text-slate-500">Cuide dos seus pets e acompanhe seus pedidos.</p>
        </div>
        <Botao variante="contorno" onClick={() => setModalSenha(true)}>
          <KeyRound className="h-4 w-4" /> Alterar senha
        </Botao>
      </div>

      {aba === 'inicio' && (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <Cartao className="flex items-center gap-3 p-4">
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                <PawPrint className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500">Meus pets</p>
                <p className="text-2xl font-semibold text-slate-900">{pets.length}</p>
              </div>
            </Cartao>
            <Cartao className="flex items-center gap-3 p-4">
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
                <Clock className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500">Pedidos pendentes</p>
                <p className="text-2xl font-semibold text-slate-900">{pendentes}</p>
              </div>
            </Cartao>
            <Cartao className="flex items-center gap-3 p-4">
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-sky-50 text-sky-700">
                <CalendarPlus className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs text-slate-500">Agendamentos confirmados</p>
                <p className="text-2xl font-semibold text-slate-900">{aprovadas}</p>
              </div>
            </Cartao>
          </div>

          <div className="grid gap-4 lg:grid-cols-3">
            <Cartao className="p-4 lg:col-span-2">
              <h2 className="text-sm font-semibold text-slate-800">Seus pets</h2>
              {carregando ? (
                <Carregando />
              ) : pets.length === 0 ? (
                <Vazio
                  titulo="Nenhum pet cadastrado"
                  descricao="Cadastre seu primeiro pet para solicitar atendimento."
                  acao={<Botao onClick={abrirNovoPet}>Cadastrar pet</Botao>}
                />
              ) : (
                <div className="mt-3 grid gap-2 sm:grid-cols-2">
                  {pets.map((pet) => (
                    <div key={pet.id} className="rounded-xl border border-slate-200 p-3">
                      <p className="font-medium text-slate-800">{pet.nome}</p>
                      <p className="text-xs text-slate-400">
                        {pet.especie} · {pet.raca || 'Raça não informada'} · {idade(pet.dataNascimento)}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </Cartao>

            <Cartao className="p-4">
              <h2 className="text-sm font-semibold text-slate-800">Ações rápidas</h2>
              <div className="mt-3 space-y-2">
                <Botao className="w-full" onClick={() => irPara('agendar')}>
                  <CalendarPlus className="h-4 w-4" /> Pedir agendamento
                </Botao>
                <Botao variante="contorno" className="w-full" onClick={() => irPara('pets')}>
                  <PawPrint className="h-4 w-4" /> Gerenciar pets
                </Botao>
                <Botao variante="contorno" className="w-full" onClick={() => irPara('pedidos')}>
                  <ClipboardList className="h-4 w-4" /> Ver pedidos
                </Botao>
              </div>
              {proxima && (
                <div className="mt-4 rounded-lg bg-brand-50 p-3 text-sm text-brand-800">
                  <p className="font-medium">Próximo atendimento</p>
                  <p className="mt-1 text-xs">
                    {proxima.animalNome} · {formatarDataHora(proxima.dataPreferida)}
                  </p>
                </div>
              )}
            </Cartao>
          </div>
        </>
      )}

      {aba === 'pets' && (
        <>
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-900">Meus pets</h2>
            <Botao onClick={abrirNovoPet}>
              <Plus className="h-4 w-4" /> Novo pet
            </Botao>
          </div>
          {pets.length === 0 ? (
            <Cartao>
              <Vazio
                titulo="Nenhum pet cadastrado"
                descricao="Cadastre seu pet para solicitar consultas."
                acao={<Botao onClick={abrirNovoPet}>Cadastrar pet</Botao>}
              />
            </Cartao>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {pets.map((pet) => (
                <Cartao key={pet.id} className="p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                      <PawPrint className="h-5 w-5" />
                    </div>
                    <button
                      onClick={() => abrirEdicaoPet(pet)}
                      className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-brand-700"
                      aria-label={`Editar ${pet.nome}`}
                    >
                      <Pencil className="h-4 w-4" />
                    </button>
                  </div>
                  <p className="mt-3 font-semibold text-slate-900">{pet.nome}</p>
                  <p className="text-sm text-slate-500">
                    {pet.especie} · {pet.raca || 'Raça não informada'}
                  </p>
                  <p className="mt-1 text-xs text-slate-400">{idade(pet.dataNascimento)}</p>
                </Cartao>
              ))}
            </div>
          )}
        </>
      )}

      {aba === 'agendar' && (
        <div className="grid gap-4 lg:grid-cols-2">
          <Cartao className="p-5">
            <h2 className="flex items-center gap-2 text-lg font-semibold text-slate-900">
              <CalendarPlus className="h-5 w-5 text-brand-600" /> Pedir agendamento
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Escolha o pet e o horário preferido. A recepção confirmará a disponibilidade.
            </p>
            <div className="mt-4 space-y-4">
              <Campo label="Pet" obrigatorio>
                <Selecao value={petSelecionado} onChange={(e) => setPetSelecionado(e.target.value)}>
                  <option value="">Selecione o pet</option>
                  {pets.map((pet) => (
                    <option key={pet.id} value={pet.id}>
                      {pet.nome} · {pet.especie}
                    </option>
                  ))}
                </Selecao>
              </Campo>
              <Campo label="Data e horário preferidos" obrigatorio>
                <Entrada
                  type="datetime-local"
                  min={paraInputDateTime(new Date())}
                  value={dataPreferida}
                  onChange={(e) => setDataPreferida(e.target.value)}
                />
              </Campo>
              <Campo label="Motivo / observação">
                <Entrada
                  value={motivo}
                  onChange={(e) => setMotivo(e.target.value)}
                  placeholder="Ex.: coceira, vacina, retorno"
                  maxLength={500}
                />
              </Campo>
              {erroPedido && (
                <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                  {erroPedido}
                </div>
              )}
              <Botao
                className="w-full"
                onClick={pedirAgendamento}
                carregando={enviandoPedido}
                disabled={pets.length === 0}
              >
                Enviar pedido
              </Botao>
              {pets.length === 0 && (
                <p className="text-center text-xs text-amber-600">
                  Cadastre um pet antes de solicitar atendimento.
                </p>
              )}
            </div>
          </Cartao>

          <Cartao className="p-5">
            <h2 className="text-sm font-semibold text-slate-800">Como funciona</h2>
            <ol className="mt-3 space-y-3 text-sm text-slate-600">
              <li className="flex gap-3">
                <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-semibold text-brand-700">
                  1
                </span>
                Você envia o pedido com pet, data e motivo.
              </li>
              <li className="flex gap-3">
                <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-semibold text-brand-700">
                  2
                </span>
                A recepção confirma o veterinário e o horário.
              </li>
              <li className="flex gap-3">
                <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-semibold text-brand-700">
                  3
                </span>
                O status do pedido aparece em “Meus pedidos”.
              </li>
            </ol>
          </Cartao>
        </div>
      )}

      {aba === 'pedidos' && (
        <Cartao>
          <div className="border-b border-slate-100 px-4 py-3">
            <h2 className="text-sm font-semibold text-slate-800">Meus pedidos de agendamento</h2>
          </div>
          {carregando ? (
            <Carregando />
          ) : solicitacoes.length === 0 ? (
            <Vazio
              titulo="Nenhum pedido"
              descricao="Você ainda não solicitou agendamento."
              acao={<Botao onClick={() => irPara('agendar')}>Pedir agendamento</Botao>}
            />
          ) : (
            <ul className="divide-y divide-slate-100">
              {solicitacoes.map((solicitacao) => (
                <li key={solicitacao.id} className="flex flex-col gap-2 px-4 py-3 sm:flex-row sm:items-center">
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-medium text-slate-800">
                      {solicitacao.animalNome} · {formatarDataHora(solicitacao.dataPreferida)}
                    </p>
                    {solicitacao.motivo && <p className="text-xs text-slate-500">{solicitacao.motivo}</p>}
                    {solicitacao.observacaoResposta && (
                      <p className="mt-1 text-xs text-slate-500">
                        Resposta da clínica: {solicitacao.observacaoResposta}
                      </p>
                    )}
                  </div>
                  <Badge
                    tom={
                      solicitacao.status === 'APROVADA'
                        ? 'verde'
                        : solicitacao.status === 'RECUSADA'
                          ? 'vermelho'
                          : 'ambar'
                    }
                  >
                    {solicitacao.status}
                  </Badge>
                </li>
              ))}
            </ul>
          )}
        </Cartao>
      )}

      <Modal
        aberto={modalPet}
        onFechar={() => setModalPet(false)}
        titulo={petEditando ? 'Editar pet' : 'Novo pet'}
        descricao="Os dados ficam vinculados ao seu cadastro."
      >
        <form onSubmit={petForm.handleSubmit(salvarPet)} className="space-y-4" noValidate>
          <Campo label="Nome" erro={petForm.formState.errors.nome?.message} obrigatorio>
            <Entrada placeholder="Thor" {...petForm.register('nome')} />
          </Campo>
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Espécie" erro={petForm.formState.errors.especie?.message} obrigatorio>
              <Selecao {...petForm.register('especie')}>
                {['Cão', 'Gato', 'Ave', 'Coelho', 'Réptil', 'Outro'].map((especie) => (
                  <option key={especie} value={especie}>
                    {especie}
                  </option>
                ))}
              </Selecao>
            </Campo>
            <Campo label="Raça" erro={petForm.formState.errors.raca?.message}>
              <Entrada placeholder="Golden Retriever" {...petForm.register('raca')} />
            </Campo>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Sexo">
              <Selecao {...petForm.register('sexo')}>
                <option value="">Não informado</option>
                <option value="Macho">Macho</option>
                <option value="Fêmea">Fêmea</option>
              </Selecao>
            </Campo>
            <Campo label="Data de nascimento">
              <Entrada type="date" max={paraInputDateTime(new Date()).slice(0, 10)} {...petForm.register('dataNascimento')} />
            </Campo>
          </div>
          {erroPet && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {erroPet}
            </div>
          )}
          <div className="flex justify-end gap-2">
            <Botao type="button" variante="contorno" onClick={() => setModalPet(false)}>
              Cancelar
            </Botao>
            <Botao type="submit" carregando={salvandoPet}>
              Salvar pet
            </Botao>
          </div>
        </form>
      </Modal>

      <Modal
        aberto={modalSenha}
        onFechar={() => {
          if (!primeiroAcesso) setModalSenha(false)
        }}
        titulo={primeiroAcesso ? 'Defina sua nova senha' : 'Alterar senha'}
        descricao={
          primeiroAcesso
            ? 'No primeiro acesso, troque a senha temporária fornecida pela clínica.'
            : 'Informe a senha atual e a nova senha.'
        }
        largura="max-w-md"
      >
        <form onSubmit={senhaForm.handleSubmit(salvarSenha)} className="space-y-4" noValidate>
          <Campo label="Senha atual" erro={senhaForm.formState.errors.atual?.message} obrigatorio>
            <Entrada type="password" autoComplete="current-password" {...senhaForm.register('atual')} />
          </Campo>
          <Campo label="Nova senha" erro={senhaForm.formState.errors.nova?.message} obrigatorio>
            <Entrada type="password" autoComplete="new-password" {...senhaForm.register('nova')} />
          </Campo>
          <Campo label="Confirmar nova senha" erro={senhaForm.formState.errors.confirmacao?.message} obrigatorio>
            <Entrada type="password" autoComplete="new-password" {...senhaForm.register('confirmacao')} />
          </Campo>
          {erroSenha && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {erroSenha}
            </div>
          )}
          <div className="flex justify-end gap-2">
            {!primeiroAcesso && (
              <Botao type="button" variante="contorno" onClick={() => setModalSenha(false)}>
                Cancelar
              </Botao>
            )}
            <Botao type="submit" carregando={salvandoSenha}>
              Salvar nova senha
            </Botao>
          </div>
        </form>
      </Modal>

    </div>
  )
}
