import { Navigate, Outlet } from 'react-router-dom'
import { useClienteAuth } from '../../context/ClienteAuthContext'
import { Carregando } from '../ui'

export function RotaCliente() {
  const { tutor, carregando } = useClienteAuth()

  if (carregando) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Carregando texto="Validando acesso..." />
      </div>
    )
  }
  if (!tutor) {
    return <Navigate to="/cliente/login" replace />
  }
  return <Outlet />
}
