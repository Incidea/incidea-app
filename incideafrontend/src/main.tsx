import ReactDOM from 'react-dom/client'
import { RouterProvider, createRouter } from '@tanstack/react-router'
import { routeTree } from './routeTree.gen'
import { supabase } from './lib/supabase'
import { clearProfile } from './features/auth/verifySession'

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