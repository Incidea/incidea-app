import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authenticated/dashboard')({
    component: RouteComponent,
})

function RouteComponent() {
    const { profile } = Route.useRouteContext()

    return (
        <div className="p-8">
            <h1 className="text-2xl font-bold">Dashboard</h1>
            <div className="mt-4 space-y-2">
                <p><strong>Nombre:</strong> {profile.name}</p>
                <p><strong>Rol:</strong> {profile.role}</p>
                <p><strong>Organización:</strong> {profile.organizationId ?? 'Sin organización'}</p>
            </div>
        </div>
    )
}
