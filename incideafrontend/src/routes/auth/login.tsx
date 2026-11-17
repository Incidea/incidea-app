import { Link, createFileRoute, redirect, useNavigate } from '@tanstack/react-router'
import { handleGoogleAuth } from '#/features/auth/authRedirects'
import { Button } from '#/features/components/ui/button'
import { Input } from '#/features/components/ui/input'
import { supabase } from '#/lib/supabase'
import React, { useState } from 'react'

// Login route: email/password + Google. Redirects to dashboard if already authenticated.
export const Route = createFileRoute('/auth/login')({
    // Sanitizes the redirect query param to avoid open redirects
    validateSearch: (search) => {
        const raw = (search.redirect as string) || '/dashboard'
        const redirectTo = raw.startsWith('/') && !raw.startsWith('//') ? raw : '/dashboard'
        return { redirect: redirectTo }
    },
    // If a session already exists, redirect away from the login page
    beforeLoad: async ({ search }) => {
        const { data: { session } } = await supabase.auth.getSession()
        if (session) {
            throw redirect({ to: search.redirect })
        }
    },
    component: LoginComponent,
})

function LoginComponent() {
    const { redirect: redirectTo } = Route.useSearch()
    const navigate = useNavigate()
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [error, setError] = useState('')

    const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
        e.preventDefault()
        setIsLoading(true)
        setError('')

        const { error: signInError } = await supabase.auth.signInWithPassword({ email, password })
        if (signInError) {
            setError(signInError.message)
            setIsLoading(false)
            return
        }

        await navigate({ to: redirectTo })
    }



    return (
        <div className="min-h-screen flex items-center justify-center">
            <form
                onSubmit={handleSubmit}
                className="max-w-md w-full space-y-4 p-6 border rounded-lg"
            >
                <h1 className="text-2xl font-bold text-center">Iniciar sesión</h1>
                <p className="text-center text-sm">
                    Accede con tu cuenta de Google. Si es tu primera vez, se creará tu cuenta automáticamente.
                </p>
                <Button type="button" onClick={() => handleGoogleAuth(redirectTo, setError)}>
                    Continuar con Google
                </Button>

                <p className="text-center text-sm">O inicia sesión con tu correo electrónico</p>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        {error}
                    </div>
                )}

                <Input
                    id="email"
                    label="Correo electrónico"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <Input
                    id="password"
                    label="Contraseña"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <Button type="submit" isSubmitting={isLoading} submittingText="Iniciando sesión...">
                    Iniciar sesión
                </Button>

                <p className="text-center text-sm">
                    ¿No tienes cuenta?{' '}
                    <Link to="/auth/register" search={{ redirect: redirectTo }} className="underline">
                        Crear cuenta
                    </Link>
                </p>
            </form>
        </div>
    )
}
