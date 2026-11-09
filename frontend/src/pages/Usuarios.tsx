import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import { formatarData } from '../lib/format'
import { camposDeErro, mensagemDeErro, usuariosApi } from '../lib/api'
import type { PageResponse, Perfil, Usuario } from '../types'
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

const schema = z
  .object({
    nome: z.string().min(3, 'Informe o nome completo'),
    email: z.string().min(1, 'Informe o e-mail').email('E-mail inválido'),
    senha: z.string().optional(),
    perfil: z.enum(['ADMINISTRADOR', 'RECEPCIONISTA', 'VETERINARIO']),
    ativo: z.boolean(),
  })
  .refine((dados) => !dados.senha || dados.senha.length >= 6, {
    path: ['senha'],
    message: 'A senha deve ter ao menos 6 caracteres',
  })

type Formulario = z.infer<typeof schema>

const vazio: Formulario = {
  nome: '',
  email: '',
  senha: '',
  perfil: 'RECEPCIONISTA',
  ativo: true,
}

const perfis: { valor: Perfil; rotulo: string }[] = [
  { valor: 'ADMINISTRADOR', rotulo: 'Administrador' },
  { valor: 'RECEPCIONISTA', rotulo: 'Recepcionista' },
  { valor: 'VETERINARIO', rotulo: 'Veterinário' },
]

export function Usuarios() {
  const { usuario } = useAuth()
  const { mostrar } = useToast()

  const [dados, setDados] = useState<PageResponse<Usuario> | null>(null)
  const [termo, setTermo] = useState('')
  const [pagina, setPagina] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<Usuario | null>(null)
  const [excluindo, setExcluindo] = useState<Usuario | null>(null)
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
      const { data } = await usuariosApi.listar(termo || undefined, pagina, 10)
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

  function abrirEdicao(item: Usuario) {
    setEditando(item)
    setErroApi('')
    reset({
      nome: item.nome,
      email: item.email,
      senha: '',
      perfil: item.perfil,
      ativo: item.ativo,
    })
    setModalAberto(true)
  }

  async function salvar(dadosForm: Formulario) {
    setSalvando(true)
    setErroApi('')
    const payload = {
      nome: dadosForm.nome.trim(),
      email: dadosForm.email.trim(),
      senha: dadosForm.senha?.trim() || undefined,
      perfil: dadosForm.perfil,
      ativo: dadosForm.ativo,
    }
    if (!editando && !payload.senha) {
      setError('senha', { message: 'Informe a senha inicial' })
      setSalvando(false)
      return
    }
    try {
      if (editando) {
        await usuariosApi.atualizar(editando.id, payload)
        mostrar('Usuário atualizado com sucesso.')
      } else {
        await usuariosApi.criar(payload)
        mostrar('Usuário cadastrado com sucesso.')
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
      await usuariosApi.excluir(excluindo.id)
      mostrar('Usuário desativado.')
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
        titulo="Usuários"
        descricao="Contas de acesso e perfis de permissão do sistema."
        acao={
          <Botao onClick={abrirNovo}>
            <Plus className="h-4 w-4" /> Novo usuário
          </Botao>
        }
      />

      <Cartao>
        <div className="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center sm:justify-between">
          <Busca valor={termo} onChange={(v) => { setTermo(v); setPagina(0) }} placeholder="Buscar por nome ou e-mail" />
          <span className="text-xs text-slate-400">Manutenção de usuários e perfis (REQ01)</span>
        </div>

        {carregando ? (
          <Carregando />
        ) : !dados || dados.content.length === 0 ? (
          <Vazio titulo="Nenhum usuário encontrado" descricao="Ajuste a busca ou cadastre um novo usuário." />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs tracking-wide text-slate-500 uppercase">
                  <tr>
                    <th className="px-4 py-3 font-medium">Usuário</th>
                    <th className="px-4 py-3 font-medium">Perfil</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                    <th className="px-4 py-3 font-medium">Criado em</th>
                    <th className="px-4 py-3 text-right font-medium">Ações</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {dados.content.map((item) => (
                    <tr key={item.id} className="hover:bg-slate-50/60">
                      <td className="px-4 py-3">
                        <p className="font-medium text-slate-800">{item.nome}</p>
                        <p className="text-xs text-slate-400">{item.email}</p>
                      </td>
                      <td className="px-4 py-3">
                        <Badge
                          tom={
                            item.perfil === 'ADMINISTRADOR'
                              ? 'azul'
                              : item.perfil === 'VETERINARIO'
                                ? 'verde'
                                : 'ambar'
                          }
                        >
                          {item.perfilDescricao}
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <Badge tom={item.ativo ? 'verde' : 'vermelho'}>
                          {item.ativo ? 'Ativo' : 'Inativo'}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-slate-500">{formatarData(item.criadoEm)}</td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-1">
                          <button
                            onClick={() => abrirEdicao(item)}
                            className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-brand-700"
                            aria-label={`Editar ${item.nome}`}
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => setExcluindo(item)}
                            disabled={item.id === usuario?.id}
                            className="rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-30"
                            aria-label={`Desativar ${item.nome}`}
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
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
        titulo={editando ? 'Editar usuário' : 'Novo usuário'}
        descricao="Defina o perfil de acesso conforme as responsabilidades do usuário."
      >
        <form onSubmit={handleSubmit(salvar)} className="space-y-4" noValidate>
          <Campo label="Nome completo" erro={errors.nome?.message} obrigatorio>
            <Entrada placeholder="Nome do usuário" {...register('nome')} />
          </Campo>
          <Campo label="E-mail" erro={errors.email?.message} obrigatorio>
            <Entrada type="email" placeholder="usuario@vetcare.com" {...register('email')} />
          </Campo>
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo
              label={editando ? 'Nova senha (opcional)' : 'Senha inicial'}
              erro={errors.senha?.message}
              obrigatorio={!editando}
            >
              <Entrada type="password" placeholder="Mínimo 6 caracteres" autoComplete="new-password" {...register('senha')} />
            </Campo>
            <Campo label="Perfil de acesso" erro={errors.perfil?.message} obrigatorio>
              <Selecao {...register('perfil')}>
                {perfis.map((perfil) => (
                  <option key={perfil.valor} value={perfil.valor}>
                    {perfil.rotulo}
                  </option>
                ))}
              </Selecao>
            </Campo>
          </div>

          <label className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700">
            <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register('ativo')} />
            Usuário ativo (pode entrar no sistema)
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
              {editando ? 'Salvar alterações' : 'Cadastrar usuário'}
            </Botao>
          </div>
        </form>
      </Modal>

      <Confirmacao
        aberto={!!excluindo}
        titulo="Desativar usuário"
        mensagem={`Deseja desativar ${excluindo?.nome ?? ''}? A conta deixa de autenticar, mas o histórico é preservado.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
        carregando={salvando}
      />
    </>
  )
}
