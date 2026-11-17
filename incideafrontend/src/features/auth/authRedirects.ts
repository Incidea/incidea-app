import { supabase } from "../../lib/supabase"

export const handleGoogleAuth = async (redirectTo: string, setError: (value: string) => void) => {
    setError('')
    const { error: oauthError } = await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: {
            redirectTo: buildAuthCallbackUrl(redirectTo),
        },
    })
    if (oauthError) setError(oauthError.message)
}

export const buildAuthCallbackUrl = (redirectTo: string) =>
    `${window.location.origin}/auth/callback?redirect=${encodeURIComponent(redirectTo)}`