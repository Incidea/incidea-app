import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/auth/onboarding')({
  component: RouteComponent,
})

function RouteComponent() {
  return <div></div>
}
