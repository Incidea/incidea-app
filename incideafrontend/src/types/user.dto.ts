export interface UserDto {
    id: string,
    name: string,
    email: string,
    role: 'CLIENT' | 'MANAGER' | 'TECHNICIAN' | 'ORGANIZATION_ADMIN' | 'PLATFORM_ADMIN',
    organizationId: string | null
}
