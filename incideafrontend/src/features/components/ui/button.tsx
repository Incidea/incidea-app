import type { ButtonHTMLAttributes } from "react"

type Props = {
    isSubmitting?: boolean
    submittingText?: string
} & ButtonHTMLAttributes<HTMLButtonElement>

export const Button = ({
    isSubmitting = false,
    submittingText = "Enviando...",
    disabled,
    children,
    ...props
}: Props) => {
    return (
        <button disabled={disabled || isSubmitting} {...props}>
            {isSubmitting ? submittingText : children}
        </button>
    )
}
