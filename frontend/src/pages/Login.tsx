import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff, LockKeyhole, Stethoscope, Zap } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { mensagemDeErro } from '../lib/api'
import { Botao, Campo, Entrada } from '../components/ui'
import { CaoCartoon, CoelhoCartoon, GatoCartoon } from '../components/AnimalArt'

const demo = [
  { perfil: 'Administrador', email: 'admin@vetcare.com', senha: 'Admin@123' },
  { perfil: 'Recepcionista', email: 'recepcao@vetcare.com', senha: 'Recepcao@123' },
  { perfil: 'Veterinário', email: 'vet@vetcare.com', senha: 'Vet@123' },
]

const schema = z.object({
  email: z.string().min(1, 'Informe o e-mail').email('E-mail inválido'),
  senha: z.string().min(1, 'Informe a senha'),
})

type Formulario = z.infer<typeof schema>


export function Login() {
  const { entrar } = useAuth()
  const mostrarDemo = import.meta.env.VITE_DEMO_LOGINS !== 'false'
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
      await entrar(dados.email, dados.senha)
      navigate('/portal', { replace: true })
    } catch (error) {
      setErro(mensagemDeErro(error))
    }
  }

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[1.1fr_1fr]">
      <div className="relative hidden overflow-hidden bg-brand-800 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="absolute -top-24 -right-24 h-72 w-72 rounded-full bg-brand-700/60 blur-2xl" />
        <div className="absolute bottom-0 -left-20 h-80 w-80 rounded-full bg-brand-600/40 blur-3xl" />
        <div className="relative flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/10">
            <Stethoscope className="h-6 w-6" />
          </div>
          <span className="text-xl font-semibold">VetCare</span>
        </div>

        <div className="relative max-w-md">
          <h1 className="text-4xl leading-tight font-semibold">
            Cuidado clínico com a rotina da clínica em ordem.
          </h1>
          <p className="mt-4 text-brand-100">
            Tutores, animais e agenda em um só lugar, com acesso controlado por perfil e
            rastreabilidade das operações.
          </p>
          <div className="mt-10 grid grid-cols-3 gap-3 text-sm">
            {[
              ['Perfis', 'RBAC'],
              ['Agenda', 'Tempo real'],
              ['Auditoria', 'Trilha'],
            ].map(([titulo, valor]) => (
              <div key={titulo} className="rounded-xl bg-white/10 p-3">
                <p className="text-brand-200">{titulo}</p>
                <p className="mt-1 font-medium">{valor}</p>
              </div>
            ))}
          </div>

          <div className="relative mt-8 flex items-end gap-3">
            <CaoCartoon size={110} className="-rotate-3 drop-shadow-lg" />
            <GatoCartoon size={96} className="translate-y-2 drop-shadow-lg" />
            <CoelhoCartoon size={86} className="drop-shadow-lg" />
          </div>
        </div>

        <p className="relative text-xs text-brand-300">
          Instituto de Computação · UFBA · Engenharia de Software 1
        </p>
      </div>

      <div className="flex min-h-screen items-center justify-center bg-slate-50 px-5 py-10 lg:min-h-0">
        <div className="w-full max-w-md">
          <div className="mb-6 flex items-center justify-between lg:hidden">
            <div className="flex items-center gap-2">
              <Stethoscope className="h-6 w-6 text-brand-600" />
              <span className="text-lg font-semibold text-slate-900">VetCare</span>
            </div>
            <div className="flex items-end">
              <CaoCartoon size={54} />
              <GatoCartoon size={48} />
            </div>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-7 shadow-sm">
            <div className="mb-6">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                <LockKeyhole className="h-5 w-5" />
              </div>
              <h2 className="text-xl font-semibold text-slate-900">Acesse sua conta</h2>
              <p className="mt-1 text-sm text-slate-500">
                Entre com as credenciais fornecidas pela clínica.
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
              <Campo label="E-mail" erro={errors.email?.message} obrigatorio>
                <Entrada
                  type="email"
                  autoComplete="email"
                  placeholder="voce@clinica.com"
                  {...register('email')}
                />
              </Campo>

              <Campo label="Senha" erro={errors.senha?.message} obrigatorio>
                <div className="relative">
                  <Entrada
                    type={mostrarSenha ? 'text' : 'password'}
                    autoComplete="current-password"
                    placeholder="••••••••"
                    {...register('senha')}
                  />
                  <button
                    type="button"
                    onClick={() => setMostrarSenha((v) => !v)}
                    className="absolute top-1/2 right-2 -translate-y-1/2 rounded p-1 text-slate-500 hover:text-slate-600"
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
          </div>

          {mostrarDemo && (
          <div className="mt-6 rounded-xl border border-dashed border-slate-300 bg-slate-50/70 p-4">
            <p className="mb-2 flex items-center gap-1.5 text-xs font-medium tracking-wide text-slate-500 uppercase">
              <Zap className="h-3.5 w-3.5 text-amber-500" /> Acesso rápido — demonstração
            </p>
            <div className="space-y-1">
              {demo.map((conta) => (
                <button
                  key={conta.email}
                  type="button"
                  onClick={() => {
                    setValue('email', conta.email)
                    setValue('senha', conta.senha)
                  }}
                  className="flex w-full items-center justify-between rounded-lg px-2 py-1.5 text-left text-xs text-slate-600 hover:bg-white"
                >
                  <span className="font-medium">{conta.perfil}</span>
                  <span className="text-slate-500">{conta.email}</span>
                </button>
              ))}
            </div>
            <p className="mt-2 text-[11px] text-slate-500">Clique para preencher. Uso apenas em demonstração.</p>
          </div>
          )}

          <p className="mt-5 text-center text-xs text-slate-500">
            É cliente da clínica?{' '}
            <a href="/cliente/login" className="font-medium text-brand-700 hover:underline">
              Acessar o portal do cliente
            </a>
          </p>
          <p className="mt-2 text-center text-xs text-slate-500">
            <a href="/privacidade" className="hover:underline">
              Política de Privacidade e LGPD
            </a>
          </p>
        </div>
      </div>
    </div>
  )
}
