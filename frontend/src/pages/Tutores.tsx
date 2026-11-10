import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import { formatarCpf, formatarTelefone } from '../lib/format'
import { camposDeErro, mensagemDeErro, tutoresApi } from '../lib/api'
import type { PageResponse, Tutor } from '../types'
import {
  AreaTexto,
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
  Vazio,
} from '../components/ui'

const schema = z.object({
  nome: z.string().min(3, 'Informe o nome completo'),
  cpf: z.string().min(11, 'CPF inválido'),
  email: z.string().min(1, 'Informe o e-mail').email('E-mail inválido'),
  telefone: z.string().min(10, 'Telefone inválido'),
  endereco: z.string().optional(),
  senha: z
    .string()
    .optional()
    .refine((valor) => !valor || valor.length >= 6, {
      message: 'A senha deve ter ao menos 6 caracteres',
    }),
  ativo: z.boolean(),
})

type Formulario = z.infer<typeof schema>

const vazio: Formulario = {
  nome: '',
  cpf: '',
  email: '',
  telefone: '',
  endereco: '',
  senha: '',
  ativo: true,
}

export function Tutores() {
  const { usuario } = useAuth()
  const { mostrar } = useToast()
  const podeEditar = usuario?.perfil !== 'VETERINARIO'

  const [dados, setDados] = useState<PageResponse<Tutor> | null>(null)
  const [termo, setTermo] = useState('')
  const [pagina, setPagina] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<Tutor | null>(null)
  const [excluindo, setExcluindo] = useState<Tutor | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [erroApi, setErroApi] = useState('')

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    setError,
    formState: { errors },
  } = useForm<Formulario>({ resolver: zodResolver(schema), defaultValues: vazio })

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const { data } = await tutoresApi.listar(termo || undefined, pagina, 10)
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

  function abrirNovo() {
    setEditando(null)
    setErroApi('')
    reset(vazio)
    setModalAberto(true)
  }

  function abrirEdicao(tutor: Tutor) {
    setEditando(tutor)
    setErroApi('')
    reset({
      nome: tutor.nome,
      cpf: formatarCpf(tutor.cpf),
      email: tutor.email,
      telefone: formatarTelefone(tutor.telefone),
      endereco: tutor.endereco ?? '',
      senha: '',
      ativo: tutor.ativo ?? true,
    })
    setModalAberto(true)
  }

  async function salvar(dadosForm: Formulario) {
    setSalvando(true)
    setErroApi('')
    const payload = {
      nome: dadosForm.nome.trim(),
      cpf: dadosForm.cpf.replace(/\D/g, ''),
      email: dadosForm.email.trim(),
      telefone: dadosForm.telefone.trim(),
      endereco: dadosForm.endereco?.trim() || null,
      senha: dadosForm.senha?.trim() || undefined,
      ativo: dadosForm.ativo,
    }
    try {
      if (editando) {
        await tutoresApi.atualizar(editando.id, payload)
        mostrar('Tutor atualizado com sucesso.')
      } else {
        await tutoresApi.criar(payload)
        mostrar('Tutor cadastrado com sucesso.')
      }
      setModalAberto(false)
      await carregar()
    } catch (error) {
      setErroApi(mensagemDeErro(error))
      const campos = camposDeErro(error)
      Object.entries(campos).forEach(([campo, mensagem]) => {
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
      await tutoresApi.excluir(excluindo.id)
      mostrar('Tutor excluído.')
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
        titulo="Tutores"
        descricao="Responsáveis pelos animais atendidos na clínica."
        acao={
          podeEditar && (
            <Botao onClick={abrirNovo}>
              <Plus className="h-4 w-4" /> Novo tutor
            </Botao>
          )
        }
      />

      <Cartao>
        <div className="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center sm:justify-between">
          <Busca valor={termo} onChange={(v) => { setTermo(v); setPagina(0) }} placeholder="Buscar por nome, CPF ou e-mail" />
          <span className="text-xs text-slate-500">Cadastro e manutenção de tutores (REQ04)</span>
        </div>

        {carregando ? (
          <Carregando />
        ) : !dados || dados.content.length === 0 ? (
          <Vazio
            titulo="Nenhum tutor encontrado"
            descricao={termo ? 'Tente outro termo de busca.' : 'Cadastre o primeiro tutor para começar.'}
            acao={podeEditar && !termo ? <Botao onClick={abrirNovo}>Cadastrar tutor</Botao> : undefined}
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs tracking-wide text-slate-500 uppercase">
                  <tr>
                    <th scope="col" className="px-4 py-3 font-medium">Nome</th>
                    <th scope="col" className="px-4 py-3 font-medium">CPF</th>
                    <th scope="col" className="px-4 py-3 font-medium">Contato</th>
                    <th scope="col" className="px-4 py-3 font-medium">Portal</th>
                    <th scope="col" className="px-4 py-3 font-medium">Endereço</th>
                    {podeEditar && <th scope="col" className="px-4 py-3 text-right font-medium">Ações</th>}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {dados.content.map((tutor) => (
                    <tr key={tutor.id} className="hover:bg-slate-50/60">
                      <td className="px-4 py-3">
                        <p className="font-medium text-slate-800">{tutor.nome}</p>
                      </td>
                      <td className="px-4 py-3 text-slate-600">{formatarCpf(tutor.cpf)}</td>
                      <td className="px-4 py-3">
                        <p className="text-slate-700">{formatarTelefone(tutor.telefone)}</p>
                        <p className="text-xs text-slate-500">{tutor.email}</p>
                      </td>
                      <td className="px-4 py-3">
                        <Badge tom={tutor.portalConfigurado ? 'verde' : 'ambar'}>
                          {tutor.portalConfigurado ? 'Ativo' : 'Sem senha'}
                        </Badge>
                      </td>
                      <td className="max-w-56 px-4 py-3 text-slate-500">
                        <span className="line-clamp-1">{tutor.endereco || '—'}</span>
                      </td>
                      {podeEditar && (
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-1">
                            <button
                              onClick={() => abrirEdicao(tutor)}
                              className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 hover:text-brand-700"
                              aria-label={`Editar ${tutor.nome}`}
                            >
                              <Pencil className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => setExcluindo(tutor)}
                              className="rounded-lg p-2 text-slate-500 hover:bg-red-50 hover:text-red-600"
                              aria-label={`Excluir ${tutor.nome}`}
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
        titulo={editando ? 'Editar tutor' : 'Novo tutor'}
        descricao="Dados do responsável pelo animal."
      >
        <form onSubmit={handleSubmit(salvar)} className="space-y-4" noValidate>
          <Campo label="Nome completo" erro={errors.nome?.message} obrigatorio>
            <Entrada placeholder="Ana Paula Souza" {...register('nome')} />
          </Campo>
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="CPF" erro={errors.cpf?.message} obrigatorio>
              <Entrada
                placeholder="000.000.000-00"
                {...register('cpf', {
                  onChange: (e) => setValue('cpf', formatarCpf(e.target.value)),
                })}
              />
            </Campo>
            <Campo label="Telefone" erro={errors.telefone?.message} obrigatorio>
              <Entrada
                placeholder="(71) 90000-0000"
                {...register('telefone', {
                  onChange: (e) => setValue('telefone', formatarTelefone(e.target.value)),
                })}
              />
            </Campo>
          </div>
          <Campo label="E-mail" erro={errors.email?.message} obrigatorio>
            <Entrada type="email" placeholder="tutor@email.com" {...register('email')} />
          </Campo>
          <Campo label="Endereço" erro={errors.endereco?.message}>
            <AreaTexto placeholder="Rua, número, bairro - cidade" rows={2} {...register('endereco')} />
          </Campo>
          <Campo
            label={editando ? 'Nova senha do portal (opcional)' : 'Senha do portal do cliente'}
            erro={errors.senha?.message}
          >
            <Entrada
              type="password"
              placeholder="Mínimo 6 caracteres"
              autoComplete="new-password"
              {...register('senha')}
            />
          </Campo>
          <label className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700">
            <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register('ativo')} />
            Tutor ativo (pode acessar o portal do cliente)
          </label>

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
              {editando ? 'Salvar alterações' : 'Cadastrar tutor'}
            </Botao>
          </div>
        </form>
      </Modal>

      <Confirmacao
        aberto={!!excluindo}
        titulo="Excluir tutor"
        mensagem={`Deseja excluir o tutor ${excluindo?.nome ?? ''}? Tutores com animais cadastrados não podem ser excluídos.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
        carregando={salvando}
      />
    </>
  )
}
