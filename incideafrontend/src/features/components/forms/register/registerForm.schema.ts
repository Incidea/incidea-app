import z from "zod"

export const registerFormSchema = z
    .object({
        email: z.string().trim().min(1, 'El correo electrónico es obligatorio').email('Introduce un correo electrónico válido'),
        password: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
        confirmPassword: z.string().min(1, 'Confirma la contraseña'),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: 'Las contraseñas no coinciden',
        path: ['confirmPassword'],
    })

export type RegisterFormValues = z.infer<typeof registerFormSchema>