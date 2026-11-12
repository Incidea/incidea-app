import { supabase } from "./supabase"


// Error thrown by apiRequest when the backend returns a non-OK status
export class ApiError extends Error {
    constructor(public status: number, public body: string) {
        super(`API error ${status}`)
        this.name = 'ApiError'
    }
}

// Reads the current Supabase access token from the session
async function getToken(): Promise<string | null> {
    const { data: { session } } = await supabase.auth.getSession()
    return session?.access_token ?? null
}

// Generic fetch wrapper: adds Bearer token, throws ApiError on non-OK responses
export async function apiRequest<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {
    const token = await getToken()

    const headers = new Headers(options.headers)
    headers.set('Content-Type', 'application/json')

    if (token) {
        headers.set('Authorization', `Bearer ${token}`)
    }

    const res = await fetch(`${path}`, {
        ...options,
        headers,
    })

    if (!res.ok) {
        const errorText = await res.text().catch(() => '')
        throw new ApiError(res.status, errorText)
    }

    if (res.status === 204) return undefined as T

    return res.json() as Promise<T>
}

export const api = {
    get: <T>(path: string) => apiRequest<T>(path, { method: 'GET' }),
    post: <T>(path: string, body?: unknown) => apiRequest<T>(path, { method: 'POST', body: JSON.stringify(body) }),
    put: <T>(path: string, body?: unknown) => apiRequest<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
    delete: <T>(path: string) => apiRequest<T>(path, { method: 'DELETE' }),
}
