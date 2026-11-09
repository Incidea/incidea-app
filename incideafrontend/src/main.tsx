import { clearProfile } from './features/auth/verifySession'
import { RouterProvider, createRouter } from '@tanstack/react-router'
import { routeTree } from './routeTree.gen'
import { supabase } from './lib/supabase'
import ReactDOM from 'react-dom/client'

const router = createRouter({
    routeTree,
    defaultPreload: 'intent',
    scrollRestoration: true,
})

declare module '@tanstack/react-router' {
    interface Register {
        router: typeof router
    }
}

// Reacts to auth events while the app is already mounted (e.g. sign-out from another tab).
// Clears the cached profile and invalidates the router so beforeLoad guards re-run.
supabase.auth.onAuthStateChange((event) => {
    if (event === 'SIGNED_OUT') {
        clearProfile()
        void router.invalidate()
    }
})

const rootElement = document.getElementById('app')!

if (!rootElement.innerHTML) {
    ReactDOM.createRoot(rootElement).render(<RouterProvider router={router} />)
}