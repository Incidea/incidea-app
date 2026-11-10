package com.incidea.incideabackend.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardingRequest(
        @NotBlank @Size(max = 40) String userName,
        @NotBlank @Size(max = 40) String organizationName
) {
}
