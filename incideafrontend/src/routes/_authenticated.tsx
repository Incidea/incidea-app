import { createFileRoute, redirect } from '@tanstack/react-router'
import { verifySession } from '#/features/auth/verifySession'

// Guard for all protected routes: redirects unauthenticated users to login,
// users without a profile to onboarding, and passes the profile down to children.
export const Route = createFileRoute('/_authenticated')({
    beforeLoad: async ({ location }) => {
        const res = await verifySession()
        if (res.status === 'unauthenticated') {
            throw redirect({ to: '/auth/login', search: { redirect: location.href } })
        }
        if (res.status === 'needs-onboarding') {
            throw redirect({ to: '/auth/onboarding' })
        }
        return { profile: res.profile }
    },
    errorComponent: () => <p>No se puede conectar con el servidor.</p>,
})