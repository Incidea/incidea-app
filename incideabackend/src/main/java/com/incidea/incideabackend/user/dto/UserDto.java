package com.incidea.incideabackend.user.dto;

import java.util.UUID;

import com.incidea.incideabackend.user.Role;

public record UserDto(
        UUID id,
        String name,
        String email,
        Role role,
        UUID organizationId
) {
}
