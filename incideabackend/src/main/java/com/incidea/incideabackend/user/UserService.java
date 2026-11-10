package com.incidea.incideabackend.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.incidea.incideabackend.common.exception.EntityNotFoundException;
import com.incidea.incideabackend.common.exception.UserAlreadyOnboardedException;
import com.incidea.incideabackend.organization.OrganizationService;
import com.incidea.incideabackend.user.dto.UserDto;
import com.incidea.incideabackend.user.request.OnboardingRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationService organizationService;

    @Autowired
    public UserService(UserRepository userRepository, OrganizationService organizationService) {
        this.userRepository = userRepository;
        this.organizationService = organizationService;
    }

    public UserService(UserRepository userRepository) {
        this(userRepository, null);
    }

    public UserDto onboard(Jwt jwt, OnboardingRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email claim is required");
        }
        return onboard(userId, email, request);
    }

    public UserDto onboard(UUID userId, String email, OnboardingRequest request) {
        if (userRepository.existsById(userId)) {
            throw new UserAlreadyOnboardedException("User already onboarded");
        }

        User user = new User();
        user.setId(userId);
        user.setName(request.userName());
        user.setEmail(email);
        user.setRole(Role.ORGANIZATION_ADMIN);
        user.setActive(true);

        try {
            return saveOnboarding(user, request.organizationName());
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyOnboardedException("User already onboarded");
        }
    }

    // TRANSACTIONAL OPERATIONS


    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toDto(user);
    }

    @Transactional
    public UserDto saveOnboarding(User user, String organizationName) {
        UUID organizationId = organizationService.createOrganization(organizationName);
        user.setOrganizationId(organizationId);
        User saved = saveUser(user);
        return toDto(saved);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.getOrganizationId());
    }
}
