import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import { formatarData, idade } from '../lib/format'
import { animaisApi, camposDeErro, mensagemDeErro, tutoresApi } from '../lib/api'
import type { Animal, PageResponse, Tutor } from '../types'
import {
  Badge,
  Botao,
  Busca,
  CabecalhoPagina,
  Campo,
  Carregando,
  Cartao,
  Confirmacao,
  Entrada,
  Modal,
  Paginacao,
  Selecao,
  Vazio,
} from '../components/ui'

const schema = z.object({
  nome: z.string().min(2, 'Informe o nome do animal'),
  especie: z.string().min(1, 'Selecione a espécie'),
  raca: z.string().optional(),
  sexo: z.string().optional(),
  dataNascimento: z.string().optional(),
  tutorId: z.string().min(1, 'Selecione o tutor'),
})

type Formulario = z.infer<typeof schema>

const vazio: Formulario = {
  nome: '',
  especie: 'Cão',
  raca: '',
  sexo: '',
  dataNascimento: '',
  tutorId: '',
}

const especies = ['Cão', 'Gato', 'Ave', 'Coelho', 'Réptil', 'Outro']

export function Animais() {
  const { usuario } = useAuth()
  const { mostrar } = useToast()
  const podeEditar = usuario?.perfil !== 'VETERINARIO'

  const [dados, setDados] = useState<PageResponse<Animal> | null>(null)
  const [tutores, setTutores] = useState<Tutor[]>([])
  const [termo, setTermo] = useState('')
  const [pagina, setPagina] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<Animal | null>(null)
  const [excluindo, setExcluindo] = useState<Animal | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [erroApi, setErroApi] = useState('')

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<Formulario>({ resolver: zodResolver(schema), defaultValues: vazio })

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await animaisApi.listar(termo || undefined, pagina, 10)
      setDados(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [termo, pagina, mostrar])

  useEffect(() => {
    const timer = window.setTimeout(carregar, 250)
    return () => window.clearTimeout(timer)
  }, [carregar])

  useEffect(() => {
    tutoresApi
      .listar(undefined, 0, 100)
      .then(({ data }) => setTutores(data.content))
      .catch(() => setTutores([]))
  }, [])

  function abrirNovo() {
    setEditando(null)
    setErroApi('')
    reset(vazio)
    setModalAberto(true)
  }

  function abrirEdicao(animal: Animal) {
    setEditando(animal)
    setErroApi('')
    reset({
      nome: animal.nome,
      especie: animal.especie,
      raca: animal.raca ?? '',
      sexo: animal.sexo ?? '',
      dataNascimento: animal.dataNascimento ?? '',
      tutorId: String(animal.tutorId),
    })
    setModalAberto(true)
  }

  async function salvar(dadosForm: Formulario) {
    setSalvando(true)
    setErroApi('')
    const payload = {
      nome: dadosForm.nome.trim(),
      especie: dadosForm.especie,
      raca: dadosForm.raca?.trim() || null,
      sexo: dadosForm.sexo || null,
      dataNascimento: dadosForm.dataNascimento || null,
      tutorId: Number(dadosForm.tutorId),
    }
    try {
      if (editando) {
        await animaisApi.atualizar(editando.id, payload)
        mostrar('Animal atualizado com sucesso.')
      } else {
        await animaisApi.criar(payload)
        mostrar('Animal cadastrado com sucesso.')
      }
      setModalAberto(false)
      await carregar()
    } catch (error) {
      setErroApi(mensagemDeErro(error))
      Object.entries(camposDeErro(error)).forEach(([campo, mensagem]) => {
        if (campo in vazio) setError(campo as keyof Formulario, { message: mensagem })
      })
    } finally {
      setSalvando(false)
    }
  }

  async function confirmarExclusao() {
    if (!excluindo) return
    setSalvando(true)
    try {
      await animaisApi.excluir(excluindo.id)
      mostrar('Animal excluído.')
      setExcluindo(null)
      await carregar()
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Animais"
        descricao="Pacientes da clínica, sempre vinculados a um tutor."
        acao={
          podeEditar && (
            <Botao onClick={abrirNovo}>
              <Plus className="h-4 w-4" /> Novo animal
            </Botao>
          )
        }
      />

      <Cartao>
        <div className="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center sm:justify-between">
          <Busca valor={termo} onChange={(v) => { setTermo(v); setPagina(0) }} placeholder="Buscar por animal ou tutor" />
          <span className="text-xs text-slate-400">Cadastro e manutenção de animais (REQ05)</span>
        </div>

        {carregando ? (
          <Carregando />
        ) : !dados || dados.content.length === 0 ? (
          <Vazio
            titulo="Nenhum animal encontrado"
            descricao={termo ? 'Tente outro termo de busca.' : 'Cadastre o primeiro animal para começar.'}
            acao={podeEditar && !termo ? <Botao onClick={abrirNovo}>Cadastrar animal</Botao> : undefined}
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs tracking-wide text-slate-500 uppercase">
                  <tr>
                    <th className="px-4 py-3 font-medium">Animal</th>
                    <th className="px-4 py-3 font-medium">Espécie</th>
                    <th className="px-4 py-3 font-medium">Tutor</th>
                    <th className="px-4 py-3 font-medium">Nascimento</th>
                    {podeEditar && <th className="px-4 py-3 text-right font-medium">Ações</th>}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {dados.content.map((animal) => (
                    <tr key={animal.id} className="hover:bg-slate-50/60">
                      <td className="px-4 py-3">
                        <p className="font-medium text-slate-800">{animal.nome}</p>
                        <p className="text-xs text-slate-400">
                          {animal.sexo || 'Sexo não informado'} · {idade(animal.dataNascimento)}
                        </p>
                      </td>
                      <td className="px-4 py-3">
                        <Badge tom="verde">{animal.especie}</Badge>
                        <p className="mt-1 text-xs text-slate-400">{animal.raca || 'Raça não informada'}</p>
                      </td>
                      <td className="px-4 py-3 text-slate-600">{animal.tutorNome}</td>
                      <td className="px-4 py-3 text-slate-500">{formatarData(animal.dataNascimento)}</td>
                      {podeEditar && (
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-1">
                            <button
                              onClick={() => abrirEdicao(animal)}
                              className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-brand-700"
                              aria-label={`Editar ${animal.nome}`}
                            >
                              <Pencil className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => setExcluindo(animal)}
                              className="rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-600"
                              aria-label={`Excluir ${animal.nome}`}
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      )}
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

      <Modal
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        titulo={editando ? 'Editar animal' : 'Novo animal'}
        descricao="O animal deve estar associado a um tutor."
      >
        <form onSubmit={handleSubmit(salvar)} className="space-y-4" noValidate>
          <Campo label="Tutor responsável" erro={errors.tutorId?.message} obrigatorio>
            <Selecao {...register('tutorId')}>
              <option value="">Selecione o tutor</option>
              {tutores.map((tutor) => (
                <option key={tutor.id} value={tutor.id}>
                  {tutor.nome}
                </option>
              ))}
            </Selecao>
          </Campo>

          <Campo label="Nome do animal" erro={errors.nome?.message} obrigatorio>
            <Entrada placeholder="Thor" {...register('nome')} />
          </Campo>

          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Espécie" erro={errors.especie?.message} obrigatorio>
              <Selecao {...register('especie')}>
                {especies.map((especie) => (
                  <option key={especie} value={especie}>
                    {especie}
                  </option>
                ))}
              </Selecao>
            </Campo>
            <Campo label="Raça" erro={errors.raca?.message}>
              <Entrada placeholder="Golden Retriever" {...register('raca')} />
            </Campo>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Sexo" erro={errors.sexo?.message}>
              <Selecao {...register('sexo')}>
                <option value="">Não informado</option>
                <option value="Macho">Macho</option>
                <option value="Fêmea">Fêmea</option>
              </Selecao>
            </Campo>
            <Campo label="Data de nascimento" erro={errors.dataNascimento?.message}>
              <Entrada type="date" max={new Date().toISOString().slice(0, 10)} {...register('dataNascimento')} />
            </Campo>
          </div>

          {tutores.length === 0 && (
            <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
              Nenhum tutor cadastrado. Cadastre um tutor antes de incluir animais.
            </p>
          )}

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
              {editando ? 'Salvar alterações' : 'Cadastrar animal'}
            </Botao>
          </div>
        </form>
      </Modal>

      <Confirmacao
        aberto={!!excluindo}
        titulo="Excluir animal"
        mensagem={`Deseja excluir o animal ${excluindo?.nome ?? ''}? O histórico permanece preservado na auditoria.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
        carregando={salvando}
      />
    </>
  )
}
