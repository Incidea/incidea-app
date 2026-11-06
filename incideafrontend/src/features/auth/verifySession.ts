import { supabase } from '#/lib/supabase'
import { api, ApiError } from '#/lib/api'
import type { UserDto } from '#/types/user.dto'

export type SessionState =
    | { status: 'unauthenticated' }
    | { status: 'needs-onboarding' }
    | { status: 'ready'; profile: UserDto }

type KnownState = Exclude<SessionState, { status: 'unauthenticated' }>

let cached: { userId: string; state: KnownState } | null = null
let inflight: Promise<SessionState> | null = null

export const clearProfile = () => {
    cached = null
    inflight = null
}
export const setProfile = (userId: string, profile: UserDto) => {
    cached = { userId, state: { status: 'ready', profile } }
}

async function fetchState(userId: string): Promise<SessionState> {
    try {
        const profile = await api.get<UserDto>('/api/v1/users/me')
        cached = { userId, state: { status: 'ready', profile } }
    } catch (err) {
        if (err instanceof ApiError && err.status === 404) {
            cached = { userId, state: { status: 'needs-onboarding' } }
        } else if (err instanceof ApiError && err.status === 401) {
            await supabase.auth.signOut()
            return { status: 'unauthenticated' }
        } else {
            throw err 
        }
    }
    return cached.state
}

export async function verifySession(): Promise<SessionState> {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) return { status: 'unauthenticated' }

    if (cached?.userId === session.user.id) return cached.state

    inflight ??= fetchState(session.user.id).finally(() => { inflight = null })
    return inflight
}
