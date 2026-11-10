package com.incidea.incideabackend.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.incidea.incideabackend.user.UserService;
import com.incidea.incideabackend.user.dto.UserDto;
import com.incidea.incideabackend.user.request.OnboardingRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getCurrentUser(UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<UserDto> onboarding(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OnboardingRequest request) {
        return ResponseEntity.ok(userService.onboard(jwt, request));
    }

}
