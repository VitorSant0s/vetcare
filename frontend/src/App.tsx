import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './components/Toast'
import { Layout } from './components/Layout'
import { RotaPerfil, RotaProtegida } from './components/guards'
import { Login } from './pages/Login'
import { Agenda } from './pages/Agenda'
import { Tutores } from './pages/Tutores'
import { Animais } from './pages/Animais'
import { Usuarios } from './pages/Usuarios'
import { NaoEncontrado } from './pages/NaoEncontrado'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route element={<RotaProtegida />}>
              <Route element={<Layout />}>
                <Route index element={<Navigate to="/agenda" replace />} />
                <Route path="/agenda" element={<Agenda />} />
                <Route path="/tutores" element={<Tutores />} />
                <Route path="/animais" element={<Animais />} />
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
