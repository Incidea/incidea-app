import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { RegisterForm } from './registerForm'

const navigateMock = vi.fn()
const signUpMock = vi.fn()

vi.mock('@tanstack/react-router', () => ({
  useNavigate: () => navigateMock,
}))

vi.mock('#/lib/supabase', () => ({
  supabase: {
    auth: {
      signUp: (...args: unknown[]) => signUpMock(...args),
    },
  },
}))

describe('RegisterForm', () => {
  beforeEach(() => {
    navigateMock.mockReset()
    signUpMock.mockReset()
  })

  async function fillAndSubmit(email = 'user@example.com') {
    const user = userEvent.setup()
    await user.type(screen.getByLabelText('Correo electrónico'), email)
    await user.type(screen.getByLabelText('Contraseña'), 'password123')
    await user.type(screen.getByLabelText('Confirmar contraseña'), 'password123')
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))
  }

  it('navega al destino cuando el registro devuelve sesión inmediata', async () => {
    signUpMock.mockResolvedValue({
      data: { session: { access_token: 'token' }, user: { id: 'u1' } },
      error: null,
    })

    const onEmailSent = vi.fn()
    render(<RegisterForm redirectTo="/dashboard" onEmailSent={onEmailSent} />)

    await fillAndSubmit()

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: '/dashboard' })
    })
    expect(onEmailSent).not.toHaveBeenCalled()
    expect(signUpMock).toHaveBeenCalledWith(
      expect.objectContaining({
        email: 'user@example.com',
        password: 'password123',
        options: expect.objectContaining({
          emailRedirectTo: expect.stringContaining('/auth/callback?redirect=%2Fdashboard'),
        }),
      }),
    )
  })

  it('muestra el flujo de confirmación cuando no hay sesión pero sí usuario', async () => {
    signUpMock.mockResolvedValue({
      data: { session: null, user: { id: 'u1' } },
      error: null,
    })

    const onEmailSent = vi.fn()
    render(<RegisterForm redirectTo="/dashboard" onEmailSent={onEmailSent} />)

    await fillAndSubmit('confirm@example.com')

    await waitFor(() => {
      expect(onEmailSent).toHaveBeenCalledWith('confirm@example.com')
    })
    expect(navigateMock).not.toHaveBeenCalled()
  })

  it('muestra error cuando el registro falla (p. ej. email ya registrado)', async () => {
    signUpMock.mockResolvedValue({
      data: { session: null, user: null },
      error: { message: 'User already registered' },
    })

    const onEmailSent = vi.fn()
    render(<RegisterForm redirectTo="/dashboard" onEmailSent={onEmailSent} />)

    await fillAndSubmit()

    await waitFor(() => {
      expect(screen.getByText('User already registered')).toBeInTheDocument()
    })
    expect(navigateMock).not.toHaveBeenCalled()
    expect(onEmailSent).not.toHaveBeenCalled()
  })
})
