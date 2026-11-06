import { supabase } from '#/lib/supabase'
import { createFileRoute, redirect } from '@tanstack/react-router'
import { useState } from 'react'

export const Route = createFileRoute('/auth/login')({
    validateSearch: (search) => {
        const raw = (search.redirect as string) || '/dashboard'
        const redirectTo = raw.startsWith('/') && !raw.startsWith('//') ? raw : '/dashboard'
        return { redirect: redirectTo }
    },
    beforeLoad: async ({ context }) => {
        const { data: { session } } = await supabase.auth.getSession()
        if (session) {
            throw redirect({ to: (context as { redirect?: string }).redirect ?? '/dashboard' })
        }
    },
    component: LoginComponent,
})

function LoginComponent() {
    const { redirect: redirectTo } = Route.useSearch()
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [error, setError] = useState('')

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setIsLoading(true)
        setError('')

        try {
            await supabase.auth.signInWithPassword({ email, password })
            await Route.useNavigate()({ to: redirectTo, search: { redirect: redirectTo } })
        } catch (err: any) {
            setError(err.message || 'Login failed')
        } finally {
            setIsLoading(false)
        }
    }

    const handleGoogleLogin = async () => {
        setError('')
        const { error: oauthError } = await supabase.auth.signInWithOAuth({
            provider: 'google',
            options: {
                redirectTo: `${window.location.origin}${redirectTo}`,
            },
        })
        if (oauthError) setError(oauthError.message)
    }

    return (
        <div className="min-h-screen flex items-center justify-center">
            <form
                onSubmit={handleSubmit}
                className="max-w-md w-full space-y-4 p-6 border rounded-lg"
            >
                <h1 className="text-2xl font-bold text-center">Sign In</h1>
                <button
                    type="button"
                    onClick={handleGoogleLogin}
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
                        Email
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
                        Password
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
                    {isLoading ? 'Signing in...' : 'Sign In'}
                </button>
            </form>
        </div>
    )
}
