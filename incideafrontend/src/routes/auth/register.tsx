import { RegisterForm } from '#/features/components/forms/register/registerForm'
import { Link, createFileRoute, redirect } from '@tanstack/react-router'
import { handleGoogleAuth } from '#/features/auth/authRedirects'
import { Button } from '#/features/components/ui/button'
import { VerifyEmailNotice } from '#/features/components/forms/register/verifyEmailNotice'
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
    const [emailSentTo, setEmailSentTo] = useState<string | null>(null)

    if (emailSentTo) {
        return <VerifyEmailNotice email={emailSentTo} redirectTo={redirectTo} />
    }

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="max-w-md w-full space-y-4 p-6 border rounded-lg">
                <h1 className="text-2xl font-bold text-center">Crear cuenta</h1>
                <p className="text-center text-sm">
                    Continúa con tu cuenta de Google. Si ya tienes una cuenta, iniciarás sesión.
                </p>
                <Button type="button" onClick={() => handleGoogleAuth(redirectTo, setError)}>
                    Continuar con Google
                </Button>

                <p className="text-center text-sm">O crea tu cuenta con tu correo electrónico</p>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        {error}
                    </div>
                )}

                <RegisterForm redirectTo={redirectTo} onEmailSent={(email) => setEmailSentTo(email)} />

                <p className="text-center text-sm">
                    ¿Ya tienes cuenta?{' '}
                    <Link to="/auth/login" search={{ redirect: redirectTo }} className="underline">
                        Iniciar sesión
                    </Link>
                </p>
            </div>
        </div>
    )
}
