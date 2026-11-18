import { beforeEach, describe, expect, it, vi } from 'vitest'

import { ApiError } from '#/lib/api'

import { clearProfile, verifySession } from './verifySession'

const getSessionMock = vi.fn()
const signOutMock = vi.fn()
const apiGetMock = vi.fn()

vi.mock('#/lib/supabase', () => ({
  supabase: {
    auth: {
      getSession: (...args: unknown[]) => getSessionMock(...args),
      signOut: (...args: unknown[]) => signOutMock(...args),
    },
  },
}))

vi.mock('#/lib/api', () => {
  class MockApiError extends Error {
    status: number
    body: string

    constructor(status: number, body: string) {
      super(`API error ${status}`)
      this.name = 'ApiError'
      this.status = status
      this.body = body
    }
  }

  return {
    ApiError: MockApiError,
    api: {
      get: (...args: unknown[]) => apiGetMock(...args),
    },
  }
})

const profile = {
  id: '11111111-1111-4111-8111-111111111111',
  name: 'Alice',
  email: 'alice@example.com',
  role: 'ORGANIZATION_ADMIN' as const,
  organizationId: '22222222-2222-4222-8222-222222222222',
}

describe('verifySession', () => {
  beforeEach(() => {
    clearProfile()
    getSessionMock.mockReset()
    signOutMock.mockReset()
    apiGetMock.mockReset()
  })

  it('devuelve unauthenticated cuando no hay sesión', async () => {
    getSessionMock.mockResolvedValue({ data: { session: null } })

    await expect(verifySession()).resolves.toEqual({ status: 'unauthenticated' })
    expect(apiGetMock).not.toHaveBeenCalled()
  })

  it('devuelve ready con el perfil cuando /me responde 200', async () => {
    getSessionMock.mockResolvedValue({ data: { session: { user: { id: 'user-1' } } } })
    apiGetMock.mockResolvedValue(profile)

    await expect(verifySession()).resolves.toEqual({ status: 'ready', profile })
    expect(apiGetMock).toHaveBeenCalledWith('/api/v1/users/me')
  })

  it('devuelve needs-onboarding cuando /me responde 404', async () => {
    getSessionMock.mockResolvedValue({ data: { session: { user: { id: 'user-2' } } } })
    apiGetMock.mockRejectedValue(new ApiError(404, 'Not found'))

    await expect(verifySession()).resolves.toEqual({ status: 'needs-onboarding' })
  })

  it('devuelve unauthenticated y cierra sesión cuando /me responde 401', async () => {
    getSessionMock.mockResolvedValue({ data: { session: { user: { id: 'user-3' } } } })
    apiGetMock.mockRejectedValue(new ApiError(401, 'Unauthorized'))
    signOutMock.mockResolvedValue({ error: null })

    await expect(verifySession()).resolves.toEqual({ status: 'unauthenticated' })
    expect(signOutMock).toHaveBeenCalled()
  })
})
