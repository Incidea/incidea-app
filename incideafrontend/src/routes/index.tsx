import { Link, createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/')({ component: Home })

function Home() {
  return (
    <main>
      <h1>Incidea</h1>
      <p>Gestiona tus incidencias de mantenimiento.</p>
      <Link to="/auth/login" search={{ redirect: '/' }}>Iniciar sesión</Link>
    </main>
  )
}
