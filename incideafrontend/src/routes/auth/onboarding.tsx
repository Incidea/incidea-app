import { clearProfile, verifySession } from '#/features/auth/verifySession'
import { OnBoardingForm } from '#/features/components/forms/onboarding/onboardingForm'
import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/auth/onboarding')({
  component: RouteComponent,
  beforeLoad: async () => {
    clearProfile()
    verifySession()
  }
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
