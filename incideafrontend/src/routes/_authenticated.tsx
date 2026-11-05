import { verifySession } from '#/features/auth/verifySession'
import { createFileRoute, redirect, isRedirect } from '@tanstack/react-router'

// src/routes/_authenticated.tsx
export const Route = createFileRoute('/_authenticated')({
  beforeLoad: async ({ location }) => {
    try {
      const user = await verifySession() // might throw on network error
      if (!user) {
        throw redirect({
          to: '/auth/login',
          search: { redirect: location.href },
        })
      }
      return { user }
    } catch (error) {
      // Re-throw redirects (they're intentional, not errors)
      if (isRedirect(error)) throw error

      // Auth check failed (network error, etc.) - redirect to login
      throw redirect({
        to: '/auth/login',
        search: { redirect: location.href },
      })
    }
  },
})