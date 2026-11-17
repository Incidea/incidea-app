import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { useEffect, useRef, useState } from 'react'
import { clearProfile, verifySession } from '#/features/auth/verifySession'
import { supabase } from '#/lib/supabase'

function sanitizeRedirect(raw: unknown): string {
  const value = typeof raw === 'string' ? raw : '/dashboard'
  return value.startsWith('/') && !value.startsWith('//') ? value : '/dashboard'
}

function getHashParams(): URLSearchParams {
  const hash = window.location.hash.startsWith('#')
    ? window.location.hash.slice(1)
    : window.location.hash
  return new URLSearchParams(hash)
}

export const Route = createFileRoute('/auth/callback')({
  validateSearch: (search) => {
    return { redirect: sanitizeRedirect(search.redirect) }
  },
  component: CallbackComponent,
})

function CallbackComponent() {
  const { redirect: redirectTo } = Route.useSearch()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const handledRef = useRef(false)

  useEffect(() => {
    if (handledRef.current) return
    handledRef.current = true

    const run = async () => {
      const searchParams = new URLSearchParams(window.location.search)
      const hashParams = getHashParams()

      const errorCode = searchParams.get('error') ?? hashParams.get('error')
      const errorDescription =
        searchParams.get('error_description') ?? hashParams.get('error_description')
      if (errorCode || errorDescription) {
        setError(errorDescription ?? errorCode ?? 'No se pudo completar la autenticación.')
        return
      }

      const code = searchParams.get('code')
      if (code) {
        const { error: exchangeError } = await supabase.auth.exchangeCodeForSession(code)
        if (exchangeError) {
          setError(exchangeError.message)
          return
        }
      }

      const {
        data: { session },
        error: sessionError,
      } = await supabase.auth.getSession()

      if (sessionError) {
        setError(sessionError.message)
        return
      }

      if (!session) {
        setError('No se pudo iniciar sesión. Vuelve a intentarlo desde el inicio de sesión.')
        return
      }

      clearProfile()

      try {
        const state = await verifySession()
        if (state.status === 'ready') {
          await navigate({ to: redirectTo, replace: true })
          return
        }
        if (state.status === 'needs-onboarding') {
          await navigate({ to: '/auth/onboarding', replace: true })
          return
        }
        await navigate({ to: '/auth/login', search: { redirect: redirectTo }, replace: true })
      } catch {
        setError('No se pudo conectar con el servidor. Inténtalo de nuevo.')
      }
    }

    void run()
  }, [navigate, redirectTo])

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="max-w-md w-full space-y-4 p-6 border rounded-lg">
          <h1 className="text-2xl font-bold text-center">No se pudo completar el acceso</h1>
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
          <div className="flex gap-2">
            <a
              href={`/auth/login?redirect=${encodeURIComponent(redirectTo)}`}
              className="block w-full text-center border py-2 px-4 rounded-md hover:bg-gray-50"
            >
              Iniciar sesión
            </a>
            <a
              href={`/auth/register?redirect=${encodeURIComponent(redirectTo)}`}
              className="block w-full text-center border py-2 px-4 rounded-md hover:bg-gray-50"
            >
              Crear cuenta
            </a>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="max-w-md w-full space-y-4 p-6 border rounded-lg text-center">
        <h1 className="text-2xl font-bold">Completando inicio de sesión…</h1>
        <p>Estamos validando tu sesión. Un momento.</p>
      </div>
    </div>
  )
}
