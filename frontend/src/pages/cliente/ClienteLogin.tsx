import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff, HeartHandshake, PawPrint } from 'lucide-react'
import { useClienteAuth } from '../../context/ClienteAuthContext'
import { mensagemDeErro } from '../../lib/api'
import { formatarCpf } from '../../lib/format'
import { Botao, Campo, Entrada } from '../../components/ui'

const schema = z.object({
  cpf: z.string().min(11, 'Informe o CPF'),
  senha: z.string().min(1, 'Informe a senha'),
})

type Formulario = z.infer<typeof schema>

export function ClienteLogin() {
  const { entrar } = useClienteAuth()
  const navigate = useNavigate()
  const [erro, setErro] = useState('')
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<Formulario>({ resolver: zodResolver(schema) })

  async function onSubmit(dados: Formulario) {
    setErro('')
    try {
      await entrar(dados.cpf.replace(/\D/g, ''), dados.senha)
      navigate('/portal/cliente', { replace: true })
    } catch (error) {
      setErro(mensagemDeErro(error))
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-brand-950 via-brand-800 to-brand-600 px-5 py-10">
      <div className="mx-auto flex min-h-[80vh] max-w-5xl items-center">
        <div className="grid w-full gap-8 lg:grid-cols-[1.1fr_1fr]">
          <div className="hidden flex-col justify-center text-white lg:flex">
            <div className="mb-8 flex items-center gap-2">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15">
                <HeartHandshake className="h-5 w-5" />
              </div>
              <span className="text-lg font-semibold">VetCare · Portal do Cliente</span>
            </div>
            <h1 className="max-w-md text-4xl leading-tight font-semibold">
              O cuidado do seu pet na palma da mão.
            </h1>
            <p className="mt-4 max-w-md text-brand-100">
              Acompanhe seus pets, solicite agendamentos e converse com a clínica de forma simples.
            </p>
            <div className="mt-10 grid max-w-md grid-cols-3 gap-3 text-sm">
              {[
                ['Pets', 'Cadastro'],
                ['Agenda', 'Pedido'],
                ['Histórico', 'Clínica'],
              ].map(([titulo, valor]) => (
                <div key={titulo} className="rounded-xl bg-white/10 p-3">
                  <p className="text-brand-200">{titulo}</p>
                  <p className="mt-1 font-medium">{valor}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-2xl bg-white p-7 shadow-2xl">
            <div className="mb-6">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                <PawPrint className="h-5 w-5" />
              </div>
              <h2 className="text-xl font-semibold text-slate-900">Acesse o portal</h2>
              <p className="mt-1 text-sm text-slate-500">
                Entre com o CPF do tutor e a senha fornecida pela clínica.
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
              <Campo label="CPF" erro={errors.cpf?.message} obrigatorio>
                <Entrada
                  placeholder="000.000.000-00"
                  inputMode="numeric"
                  {...register('cpf', {
                    onChange: (e) => setValue('cpf', formatarCpf(e.target.value)),
                  })}
                />
              </Campo>
              <Campo label="Senha" erro={errors.senha?.message} obrigatorio>
                <div className="relative">
                  <Entrada
                    type={mostrarSenha ? 'text' : 'password'}
                    placeholder="••••••••"
                    {...register('senha')}
                  />
                  <button
                    type="button"
                    onClick={() => setMostrarSenha((v) => !v)}
                    className="absolute top-1/2 right-2 -translate-y-1/2 rounded p-1 text-slate-400 hover:text-slate-600"
                    aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                  >
                    {mostrarSenha ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </Campo>

              {erro && (
                <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                  {erro}
                </div>
              )}

              <Botao type="submit" className="w-full" carregando={isSubmitting}>
                Entrar
              </Botao>
            </form>

            <p className="mt-5 text-center text-xs text-slate-400">
              É da equipe da clínica?{' '}
              <a href="/login" className="font-medium text-brand-700 hover:underline">
                Acessar o sistema interno
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
