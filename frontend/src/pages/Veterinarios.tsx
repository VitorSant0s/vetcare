import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { BadgeCheck, Pencil, Plus, Stethoscope, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/Toast'
import { camposDeErro, mensagemDeErro, veterinariosApi } from '../lib/api'
import type { Veterinario } from '../types'
import {
  Badge,
  Botao,
  CabecalhoPagina,
  Campo,
  Carregando,
  Cartao,
  Confirmacao,
  Entrada,
  Modal,
  Vazio,
} from '../components/ui'

const schema = z
  .object({
    nome: z.string().min(3, 'Informe o nome completo'),
    email: z.string().min(1, 'Informe o e-mail').email('E-mail inválido'),
    senha: z.string().optional(),
    crmv: z.string().min(3, 'Informe o CRMV'),
    especialidade: z.string().optional(),
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
  crmv: '',
  especialidade: '',
  ativo: true,
}

export function Veterinarios() {
  const { usuario } = useAuth()
  const { mostrar } = useToast()
  const podeEditar = usuario?.perfil === 'ADMINISTRADOR'

  const [dados, setDados] = useState<Veterinario[]>([])
  const [carregando, setCarregando] = useState(true)
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<Veterinario | null>(null)
  const [excluindo, setExcluindo] = useState<Veterinario | null>(null)
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
      const { data } = await veterinariosApi.listar(true)
      setDados(data)
    } catch (error) {
      mostrar(mensagemDeErro(error), 'erro')
    } finally {
      setCarregando(false)
    }
  }, [mostrar])

  useEffect(() => {
    carregar()
  }, [carregar])

  function abrirNovo() {
    setEditando(null)
    setErroApi('')
    reset(vazio)
    setModalAberto(true)
  }

  function abrirEdicao(item: Veterinario) {
    setEditando(item)
    setErroApi('')
    reset({
      nome: item.nome,
      email: item.email,
      senha: '',
      crmv: item.crmv ?? '',
      especialidade: item.especialidade ?? '',
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
      crmv: dadosForm.crmv.trim(),
      especialidade: dadosForm.especialidade?.trim() || null,
      ativo: dadosForm.ativo,
    }
    if (!editando && !payload.senha) {
      setError('senha', { message: 'Informe a senha inicial' })
      setSalvando(false)
      return
    }
    try {
      if (editando) {
        await veterinariosApi.atualizar(editando.id, payload)
        mostrar('Veterinário atualizado com sucesso.')
      } else {
        await veterinariosApi.criar(payload)
        mostrar('Veterinário cadastrado com sucesso.')
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
      await veterinariosApi.excluir(excluindo.id)
      mostrar('Veterinário desativado.')
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
        titulo="Veterinários"
        descricao="Profissionais da clínica, com CRMV e especialidade."
        acao={
          podeEditar && (
            <Botao onClick={abrirNovo}>
              <Plus className="h-4 w-4" /> Novo veterinário
            </Botao>
          )
        }
      />

      <Cartao>
        <div className="flex items-center justify-between border-b border-slate-100 p-4">
          <span className="text-sm text-slate-500">
            {dados.filter((v) => v.ativo).length} profissionais ativos
          </span>
          <span className="text-xs text-slate-500">Manutenção de veterinários (REQ06)</span>
        </div>

        {carregando ? (
          <Carregando />
        ) : dados.length === 0 ? (
          <Vazio
            titulo="Nenhum veterinário cadastrado"
            descricao="Cadastre os profissionais que realizam atendimentos na clínica."
            acao={podeEditar ? <Botao onClick={abrirNovo}>Cadastrar veterinário</Botao> : undefined}
          />
        ) : (
          <div className="divide-y divide-slate-100">
            {dados.map((vet) => (
              <div key={vet.id} className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                  <Stethoscope className="h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="font-medium text-slate-800">{vet.nome}</p>
                    <Badge tom={vet.ativo ? 'verde' : 'vermelho'}>
                      {vet.ativo ? 'Ativo' : 'Inativo'}
                    </Badge>
                  </div>
                  <p className="text-sm text-slate-500">{vet.email}</p>
                </div>
                <div className="flex flex-wrap gap-4 text-sm sm:w-80">
                  <div>
                    <p className="flex items-center gap-1 text-xs text-slate-500">
                      <BadgeCheck className="h-3.5 w-3.5" /> CRMV
                    </p>
                    <p className="text-slate-700">{vet.crmv || '—'}</p>
                  </div>
                  <div>
                    <p className="text-xs text-slate-500">Especialidade</p>
                    <p className="text-slate-700">{vet.especialidade || 'Clínica geral'}</p>
                  </div>
                </div>
                {podeEditar && (
                  <div className="flex gap-1">
                    <button
                      onClick={() => abrirEdicao(vet)}
                      className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 hover:text-brand-700"
                      aria-label={`Editar ${vet.nome}`}
                    >
                      <Pencil className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => setExcluindo(vet)}
                      disabled={!vet.ativo}
                      className="rounded-lg p-2 text-slate-500 hover:bg-red-50 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-30"
                      aria-label={`Desativar ${vet.nome}`}
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </Cartao>

      <Modal
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        titulo={editando ? 'Editar veterinário' : 'Novo veterinário'}
        descricao="Dados profissionais e acesso ao sistema."
      >
        <form onSubmit={handleSubmit(salvar)} className="space-y-4" noValidate>
          <Campo label="Nome completo" erro={errors.nome?.message} obrigatorio>
            <Entrada placeholder="Dra. Camila Andrade" {...register('nome')} />
          </Campo>
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="CRMV" erro={errors.crmv?.message} obrigatorio>
              <Entrada placeholder="CRMV-BA 12345" {...register('crmv')} />
            </Campo>
            <Campo label="Especialidade" erro={errors.especialidade?.message}>
              <Entrada placeholder="Clínica de pequenos animais" {...register('especialidade')} />
            </Campo>
          </div>
          <Campo label="E-mail" erro={errors.email?.message} obrigatorio>
            <Entrada type="email" placeholder="vet@vetcare.com" {...register('email')} />
          </Campo>
          <Campo
            label={editando ? 'Nova senha (opcional)' : 'Senha inicial'}
            erro={errors.senha?.message}
            obrigatorio={!editando}
          >
            <Entrada type="password" placeholder="Mínimo 6 caracteres" autoComplete="new-password" {...register('senha')} />
          </Campo>

          <label className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2.5 text-sm text-slate-700">
            <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register('ativo')} />
            Profissional ativo (pode acessar o sistema e receber consultas)
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
              {editando ? 'Salvar alterações' : 'Cadastrar veterinário'}
            </Botao>
          </div>
        </form>
      </Modal>

      <Confirmacao
        aberto={!!excluindo}
        titulo="Desativar veterinário"
        mensagem={`Deseja desativar ${excluindo?.nome ?? ''}? Profissionais com consultas futuras não podem ser desativados.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setExcluindo(null)}
        carregando={salvando}
      />
    </>
  )
}
