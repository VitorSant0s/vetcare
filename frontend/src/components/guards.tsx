import { Navigate, Outlet, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../context/AuthContext'
import { Carregando } from './ui'
import type { Perfil } from '../types'

export function RotaProtegida() {  const { usuario, carregando } = useAuth()
  const location = useLocation()

  if (carregando) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Carregando texto="Validando sessão..." />
      </div>
    )
  }
  if (!usuario) {
    return <Navigate to="/login" state={{ de: location.pathname }} replace />
  }
  return <Outlet />
}

export function RotaPerfil({ perfil, children }: { perfil: Perfil; children: ReactNode }) {
  const { usuario } = useAuth()
  if (usuario?.perfil !== perfil) {
    return (
      <div className="mx-auto max-w-lg px-6 py-20 text-center">
        <p className="text-lg font-semibold text-slate-800">Acesso restrito</p>
        <p className="mt-2 text-sm text-slate-500">
          Seu perfil não tem permissão para acessar esta área.
        </p>
      </div>
    )
  }
  return <>{children}</>
}

/**
 * Mesmo login para todos; após autenticar, o usuário é levado ao portal do seu perfil.
 */
export function RedirecionarPortal() {
  const { usuario } = useAuth()
  if (!usuario) return <Navigate to="/login" replace />
  const destino = portalDoPerfil(usuario.perfil)
  return <Navigate to={destino} replace />
}

export function RotaPapeis({ papeis, children }: { papeis: Perfil[]; children: ReactNode }) {
  const { usuario } = useAuth()
  if (!usuario || !papeis.includes(usuario.perfil)) {
    return (
      <div className="mx-auto max-w-lg px-6 py-20 text-center">
        <p className="text-lg font-semibold text-slate-800">Acesso restrito</p>
        <p className="mt-2 text-sm text-slate-500">
          Seu perfil não tem permissão para acessar esta área.
        </p>
      </div>
    )
  }
  return <>{children}</>
}

export function portalDoPerfil(perfil: Perfil): string {
  return portalPorPerfil[perfil]
}

const portalPorPerfil: Record<Perfil, string> = {
  ADMINISTRADOR: '/portal/admin',
  RECEPCIONISTA: '/portal/recepcao',
  VETERINARIO: '/portal/veterinario',
}
