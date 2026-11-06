import { createFileRoute, redirect } from '@tanstack/react-router'
import { verifySession } from '#/features/auth/verifySession'
import type { UserDto } from '#/types/user.dto'

export interface RouterContext {
    auth: {
        profile: UserDto | null
    }
}

export const Route = createFileRoute('/_authenticated')({
    beforeLoad: async ({ location }) => {
        const r = await verifySession()
        if (r.status === 'unauthenticated') {
            throw redirect({ to: '/auth/login', search: { redirect: location.href } })
        }
        if (r.status === 'needs-onboarding') {
            throw redirect({ to: '/auth/onboarding' })
        }
        return { profile: r.profile }
    },
    errorComponent: () => <p>No se puede conectar con el servidor.</p>,
})