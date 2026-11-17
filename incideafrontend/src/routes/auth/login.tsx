import { Link, createFileRoute, redirect, useNavigate } from '@tanstack/react-router'
import { handleGoogleAuth } from '#/features/auth/authRedirects'
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

                <div>
                    <label htmlFor="email" className="block text-sm font-medium mb-1">
                        Correo electrónico
                    </label>
                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                    />
                </div>

                <div>
                    <label htmlFor="password" className="block text-sm font-medium mb-1">
                        Contraseña
                    </label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                    />
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:opacity-50"
                >
                    {isLoading ? 'Iniciando sesión...' : 'Iniciar sesión'}
                </button>

                <p className="text-center text-sm">
                    ¿No tienes cuenta?{' '}
                    <Link to="/auth/register" search={{ redirect: redirectTo }} className="underline">
                        Regístrate
                    </Link>
                </p>
            </form>
        </div>
    )
}
