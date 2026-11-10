package com.incidea.incideabackend.user.dto;

import com.incidea.incideabackend.user.Role;

/**
 * Result of the account status query: user and organization in a single query.
 * {@code organizationActive} is {@code null} when the user has no organization.
 * For {@code PLATFORM_ADMIN}, only {@code active} (user) is validated, not the organization.
 */
public record AccountStatus(boolean active, Role role, Boolean organizationActive) {
}
