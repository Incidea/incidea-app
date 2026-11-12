import type { UseFormRegisterReturn } from "react-hook-form"
import type { HTMLInputTypeAttribute } from "react"

type Props = {
    id: string,
    label: string
    register?: UseFormRegisterReturn,
    error?: string,
}

export const Input = ({ id, register, error, label, ...props }: Props) => {
    const errorId = `${id}-error`
    return (
        <div>
            <label htmlFor={id}>{label}</label>
            <input
                id={id}
                aria-invalid={!!error}
                aria-describedby={error ? errorId : undefined}
                {...register}
                {...props}
            />
            {error && <p>{error}</p>}
        </div>
    )
}