import ReactDOM from 'react-dom/client'
import { RouterProvider, createRouter } from '@tanstack/react-router'
import { routeTree } from './routeTree.gen'
import { SupabaseAuthProvider, useSupabaseAuth } from './lib/supabase-auth'
import { useEffect } from 'react'

const router = createRouter({
  routeTree,
  defaultPreload: 'intent',
  scrollRestoration: true,
  context: { auth: undefined! },
})

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

function InnerApp() {
  const auth = useSupabaseAuth()

  // Change the sesion execte all beforeLoad
  useEffect(() => {
    if (auth.isLoading) return // There arent sesion now
    router.update({ context: { auth } }) // Router has the actual auth
    router.invalidate()
  }, [auth.isAuthenticated, auth.isLoading])

  // Waiting for supabase sesion
  if (auth.isLoading) return null // or spinner

  return <RouterProvider router={router} context={{ auth }} />
}

function App() {
  return (
    <SupabaseAuthProvider>
      <InnerApp />
    </SupabaseAuthProvider>
  )
}

const rootElement = document.getElementById('app')!

if (!rootElement.innerHTML) {
  const root = ReactDOM.createRoot(rootElement)
  root.render(<App />)
}