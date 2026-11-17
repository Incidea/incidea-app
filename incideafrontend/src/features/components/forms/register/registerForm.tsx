import { Button } from "#/features/components/ui/button"
import { Input } from "#/features/components/ui/input"
import { supabase } from "#/lib/supabase"
import { zodResolver } from "@hookform/resolvers/zod"
import { useNavigate } from "@tanstack/react-router"
import { useState } from "react"
import type { SubmitHandler } from "react-hook-form"
import { useForm } from "react-hook-form"
import type { RegisterFormValues } from "./registerForm.schema"
import { registerFormSchema } from "./registerForm.schema"
import { buildAuthCallbackUrl } from "#/features/auth/authRedirects"

type Props = {
    redirectTo: string
    onEmailSent: () => void
}

export const RegisterForm = ({ redirectTo, onEmailSent }: Props) => {
    const navigate = useNavigate()
    const [submitError, setSubmitError] = useState("")

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<RegisterFormValues>({
        resolver: zodResolver(registerFormSchema),
        defaultValues: { email: "", password: "", confirmPassword: "" },
    })

    const onSubmit: SubmitHandler<RegisterFormValues> = async (values) => {
        setSubmitError("")

        const { data, error } = await supabase.auth.signUp({
            email: values.email,
            password: values.password,
            options: {
                emailRedirectTo: buildAuthCallbackUrl(redirectTo),
            },
        })

        if (error) {
            setSubmitError(error.message)
            return
        }

        if (data.session) {
            await navigate({ to: redirectTo })
            return
        }

        if (data.user) {
            onEmailSent()
        }
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {submitError && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                    {submitError}
                </div>
            )}

            <Input
                id="email"
                label="Correo electrónico"
                type="email"
                {...register("email")}
                error={errors.email?.message}
            />
            <Input
                id="password"
                label="Contraseña"
                type="password"
                {...register("password")}
                error={errors.password?.message}
            />
            <Input
                id="confirmPassword"
                label="Confirmar contraseña"
                type="password"
                {...register("confirmPassword")}
                error={errors.confirmPassword?.message}
            />

            <Button type="submit" isSubmitting={isSubmitting} submittingText="Creando cuenta...">
                Crear cuenta
            </Button>
        </form>
    )
}
