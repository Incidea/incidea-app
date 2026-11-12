import type { Role } from "./role.dto"

export interface UserDto {
    id: string,
    name: string,
    email: string,
    role: Role,
    organizationId: string | null
}