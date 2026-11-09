import { Link } from 'react-router-dom'
import { PawPrint } from 'lucide-react'

export function NaoEncontrado() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-slate-50 px-6 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-50 text-brand-700">
        <PawPrint className="h-7 w-7" />
      </div>
      <h1 className="text-2xl font-semibold text-slate-900">Página não encontrada</h1>
      <p className="text-sm text-slate-500">O endereço acessado não existe no VetCare.</p>
      <Link
        to="/agenda"
        className="mt-2 rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700"
      >
        Voltar para a agenda
      </Link>
    </div>
  )
}
