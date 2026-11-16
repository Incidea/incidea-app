import z from "zod";

export const onboardingFormSchema = z.object({
    userName: z
        .string()
        .trim()
        .min(1, "Es un campo obligatorio")
        .max(40, "No puede superar los 40 caracteres"),
    organizationName: z
        .string()
        .trim()
        .min(1, "Es un campo obligatorio")
        .max(40, "No puede superar los 40 caracteres"),
})

export type OnboardingFormValues = z.infer<typeof onboardingFormSchema>