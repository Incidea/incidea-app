import { RegisterForm } from '#/features/components/forms/register/registerForm'
import { Link, createFileRoute, redirect } from '@tanstack/react-router'
import { handleGoogleAuth } from '#/features/auth/authRedirects'
import { supabase } from '#/lib/supabase'
import { useState } from 'react'

export const Route = createFileRoute('/auth/register')({
    validateSearch: (search) => {
        const raw = (search.redirect as string) || '/dashboard'
        const redirectTo = raw.startsWith('/') && !raw.startsWith('//') ? raw : '/dashboard'
        return { redirect: redirectTo }
    },
    beforeLoad: async ({ search }) => {
        const { data: { session } } = await supabase.auth.getSession()
        if (session) {
            throw redirect({ to: search.redirect })
        }
    },
    component: RegisterComponent,
})

function RegisterComponent() {
    const { redirect: redirectTo } = Route.useSearch()
    const [error, setError] = useState('')
    const [emailSent, setEmailSent] = useState(false)

    if (emailSent) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="max-w-md w-full space-y-4 p-6 border rounded-lg">
                    <h1 className="text-2xl font-bold text-center">Revisa tu correo</h1>
                    <p className="text-center">
                        Te hemos enviado un enlace para confirmar tu cuenta. Después de confirmarla, inicia sesión para continuar.
                    </p>
                    <Link
                        to="/auth/login"
                        search={{ redirect: redirectTo }}
                        className="block w-full text-center border py-2 px-4 rounded-md hover:bg-gray-50"
                    >
                        Ir a iniciar sesión
                    </Link>
                </div>
            </div>
        )
    }

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="max-w-md w-full space-y-4 p-6 border rounded-lg">
                <h1 className="text-2xl font-bold text-center">Crear cuenta</h1>
                <p className="text-center text-sm">
                    También puedes crear tu cuenta con Google.
                </p>
                <button
                    type="button"
                    onClick={() => handleGoogleAuth(redirectTo, setError)}
                    className="w-full border py-2 px-4 rounded-md hover:bg-gray-50"
                >
                    Continuar con Google
                </button>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        {error}
                    </div>
                )}

                <RegisterForm redirectTo={redirectTo} onEmailSent={() => setEmailSent(true)} />

                <p className="text-center text-sm">
                    ¿Ya tienes cuenta?{' '}
                    <Link to="/auth/login" search={{ redirect: redirectTo }} className="underline">
                        Inicia sesión
                    </Link>
                </p>
            </div>
        </div>
    )
}
