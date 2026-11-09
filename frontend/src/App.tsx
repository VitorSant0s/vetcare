import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './components/Toast'
import { Layout } from './components/Layout'
import { RedirecionarPortal, RotaPerfil, RotaProtegida } from './components/guards'
import { Login } from './pages/Login'
import { Agenda } from './pages/Agenda'
import { Tutores } from './pages/Tutores'
import { Animais } from './pages/Animais'
import { Usuarios } from './pages/Usuarios'
import { Veterinarios } from './pages/Veterinarios'
import { NaoEncontrado } from './pages/NaoEncontrado'
import { PortalAdmin } from './pages/portal/PortalAdmin'
import { PortalRecepcao } from './pages/portal/PortalRecepcao'
import { PortalVeterinario } from './pages/portal/PortalVeterinario'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route element={<RotaProtegida />}>
              <Route path="/portal" element={<RedirecionarPortal />} />
              <Route element={<Layout />}>
                <Route index element={<Navigate to="/portal" replace />} />
                <Route
                  path="/portal/admin"
                  element={
                    <RotaPerfil perfil="ADMINISTRADOR">
                      <PortalAdmin />
                    </RotaPerfil>
                  }
                />
                <Route
                  path="/portal/recepcao"
                  element={
                    <RotaPerfil perfil="RECEPCIONISTA">
                      <PortalRecepcao />
                    </RotaPerfil>
                  }
                />
                <Route
                  path="/portal/veterinario"
                  element={
                    <RotaPerfil perfil="VETERINARIO">
                      <PortalVeterinario />
                    </RotaPerfil>
                  }
                />
                <Route path="/agenda" element={<Agenda />} />
                <Route path="/tutores" element={<Tutores />} />
                <Route path="/animais" element={<Animais />} />
                <Route path="/veterinarios" element={<Veterinarios />} />
                <Route
                  path="/usuarios"
                  element={
                    <RotaPerfil perfil="ADMINISTRADOR">
                      <Usuarios />
                    </RotaPerfil>
                  }
                />
              </Route>
            </Route>
            <Route path="*" element={<NaoEncontrado />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
