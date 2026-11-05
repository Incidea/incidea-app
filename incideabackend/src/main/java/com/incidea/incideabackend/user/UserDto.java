package com.incidea.incideabackend.user;

import java.util.UUID;

public record UserDto(
        UUID id,
        String name,
        String email,
        Role role,
        UUID organizationId
        ) {}
