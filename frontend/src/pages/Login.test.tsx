import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { Login } from '../pages/Login'
import { AuthProvider } from '../context/AuthContext'
import { ToastProvider } from '../components/Toast'

function renderizarLogin() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <ToastProvider>
          <Login />
        </ToastProvider>
      </AuthProvider>
    </MemoryRouter>,
  )
}

describe('tela de login', () => {
  it('exibe o formulário de acesso', () => {
    renderizarLogin()
    expect(screen.getByRole('heading', { name: /acesse sua conta/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/^e-mail/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^senha/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument()
  })


})
