import { OnBoardingForm } from '#/features/components/forms/onboardingForm'
import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/auth/onboarding')({
  component: RouteComponent,
})

function RouteComponent() {
  return <main>
    <section>
      <p>Unete a una organización</p>
    </section>
    <section>
      <p>Crea tu organización</p>
      <OnBoardingForm />
    </section>
  </main>
}
