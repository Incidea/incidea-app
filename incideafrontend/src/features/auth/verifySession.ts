import { supabase } from '#/lib/supabase'
import { api, ApiError } from '#/lib/api'
import type { UserDto } from '#/types/user.dto'

export type SessionState =
    | { status: 'unauthenticated' }
    | { status: 'needs-onboarding' }
    | { status: 'ready'; profile: UserDto }

// All SessionState variants except 'unauthenticated'
type KnownState = Exclude<SessionState, { status: 'unauthenticated' }>

// In-memory cache: user id -> last known session state
let cached: { userId: string; state: KnownState } | null = null
// Deduplicates concurrent calls to /api/v1/users/me for the same user
let inflight: Promise<SessionState> | null = null

// Clears the cache, used when the user signs out
export const clearProfile = () => {
    cached = null
    inflight = null
}

// Stores a successful profile lookup in the cache
export const setProfile = (userId: string, profile: UserDto) => {
    cached = { userId, state: { status: 'ready', profile } }
}

// Calls GET /api/v1/users/me and caches the result by user id
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

// Single source of truth for session and profile.
// Returns unauthenticated / needs-onboarding / ready with the user profile.
export async function verifySession(): Promise<SessionState> {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) return { status: 'unauthenticated' }

    // Return cached result if the user hasn't changed
    if (cached?.userId === session.user.id) return cached.state

    // Deduplicate concurrent requests for the same user
    inflight ??= fetchState(session.user.id).finally(() => { inflight = null })
    return inflight
}
