import { Button } from '#/features/components/ui/button'
import { buildAuthCallbackUrl } from '#/features/auth/authRedirects'
import { supabase } from '#/lib/supabase'
import { Link } from '@tanstack/react-router'
import { useState } from 'react'

type Props = {
    email: string
    redirectTo: string
}

export const VerifyEmailNotice = ({ email, redirectTo }: Props) => {
    const [resendStatus, setResendStatus] = useState('')
    const [resendError, setResendError] = useState('')
    const [isResending, setIsResending] = useState(false)

    const handleResendEmail = async () => {
        setIsResending(true)
        setResendStatus('')
        setResendError('')

        const { error: resendErr } = await supabase.auth.resend({
            type: 'signup',
            email,
            options: {
                emailRedirectTo: buildAuthCallbackUrl(redirectTo),
            },
        })

        if (resendErr) {
            setResendError(resendErr.message)
        } else {
            setResendStatus('Correo de confirmación reenviado. Revisa tu bandeja de entrada.')
        }

        setIsResending(false)
    }

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="max-w-md w-full space-y-4 p-6 border rounded-lg">
                <h1 className="text-2xl font-bold text-center">Revisa tu correo</h1>
                <p className="text-center">
                    Hemos enviado un enlace de confirmación a <strong>{email}</strong>. Confirma tu correo para continuar con el acceso.
                </p>

                {resendStatus && (
                    <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
                        {resendStatus}
                    </div>
                )}
                {resendError && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        {resendError}
                    </div>
                )}

                <Button
                    type="button"
                    onClick={handleResendEmail}
                    isSubmitting={isResending}
                    submittingText="Reenviando..."
                >
                    Reenviar correo
                </Button>

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
