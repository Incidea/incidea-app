import { clearProfile, setProfile } from "#/features/auth/verifySession";
import type { OnboardingFormValues } from "./onboardingForm.schema";
import { onboardingFormSchema } from "./onboardingForm.schema";
import { Button } from "#/features/components/ui/button";
import { Input } from "#/features/components/ui/input";
import type { SubmitHandler } from "react-hook-form";
import { useNavigate } from "@tanstack/react-router";
import type { UserDto } from "#/types/user.dto";
import { useForm } from "react-hook-form";
import { api, ApiError } from "#/lib/api";
import { zodResolver } from "@hookform/resolvers/zod";



export const OnBoardingForm = () => {

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<OnboardingFormValues>({
        resolver:
            zodResolver(onboardingFormSchema),
        defaultValues: {
            userName: "", organizationName: ""
        }
    });
    const navigate = useNavigate()

    const onSubmit: SubmitHandler<OnboardingFormValues> = async (data) => {
        try {
            const onboarding = await api.post<UserDto>("/api/v1/users/onboarding", data)
            setProfile(onboarding.id, onboarding)
            await navigate({ to: "/dashboard" })
        } catch (error) {

            if (!(error instanceof ApiError)) {
                console.error('Error de conexión')
                return
            }

            if (error instanceof ApiError) {
                switch (error.status) {
                    case 409:
                        clearProfile()
                        await navigate({ to: "/dashboard" })
                        break;
                    case 401:
                        await navigate({ to: "/auth/login", search: { redirect: '/dashboard' } })
                        break;
                    default:
                        console.error("Error al completar el onboarding")
                        break;

                }
            }
        }

    }

    return <form onSubmit={handleSubmit(onSubmit)}>
        <Input
            id="userName"
            type="text"
            label="Nombre y apellidos"
            {...register("userName")}
            error={errors.userName?.message}
        />
        <Input
            id="organizationName"
            type="text"
            label="Nombre de la organización"
            {...register("organizationName")}
            error={errors.organizationName?.message}
        />
        <Button type="submit" isSubmitting={isSubmitting}>Enviar</Button>
    </form >
}