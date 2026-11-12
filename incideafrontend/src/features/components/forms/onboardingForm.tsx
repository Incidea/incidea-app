import { clearProfile, setProfile } from "#/features/auth/verifySession";
import type { UserDto } from "#/types/user.dto";
import type { SubmitHandler } from "react-hook-form";
import { useNavigate } from "@tanstack/react-router";
import { useForm } from "react-hook-form";
import { api, ApiError } from "#/lib/api";

type FormValues = {
    userName: string,
    organizationName: string
}

export const OnBoardingForm = () => {

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>();
    const navigate = useNavigate()

    const onSubmit: SubmitHandler<FormValues> = async (data) => {
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
        <label htmlFor="userName">
            Nombre y apellidos
        </label>
        <input
            id="userName"
            type="text"
            {...register("userName", {
                required: "El nombre y apellidos es obligatorio",

            })}
        ></input>
        <p>{errors.organizationName?.message}</p>
        <label htmlFor="organizationName">
            Nombre de la organización
        </label>
        <input
            id="organizationName"
            type="text"
            {...register("organizationName", {
                required: "El nombre de la organización es obligatorio",
            })}
        >
        </input>
        <p>{errors.organizationName?.message}</p>
        <button type="submit" disabled={isSubmitting}>{isSubmitting ? "Enviando..." : "Enviar"}</button>
    </form >
}