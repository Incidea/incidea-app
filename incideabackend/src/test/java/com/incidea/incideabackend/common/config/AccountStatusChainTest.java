package com.incidea.incideabackend.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import com.incidea.incideabackend.common.exception.AccountDesactivateException;
import com.incidea.incideabackend.common.exception.EntityNotFoundException;
import com.incidea.incideabackend.user.dto.AccountStatus;
import com.incidea.incideabackend.user.Role;
import com.incidea.incideabackend.user.User;
import com.incidea.incideabackend.user.controller.UserController;
import com.incidea.incideabackend.user.dto.UserDto;
import com.incidea.incideabackend.user.UserRepository;
import com.incidea.incideabackend.user.UserService;

/**
 * Tests the account status validation chain:
 * {@link JwtAuthenticationStatusConverter} → exception behavior →
 * {@link UserController#me(Jwt)}.
 *
 * <p>Covers: inactive user, inactive organization, PLATFORM_ADMIN (user-only check),
 * active user, onboarding (no user row), and non-UUID subject.</p>
 */
class AccountStatusChainTest {

    private UserRepository userRepository;
    private JwtAuthenticationStatusConverter converter;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        converter = new JwtAuthenticationStatusConverter(userRepository);
        controller = new UserController(new UserService(userRepository));
        SecurityContextHolder.clearContext();
    }

    private Jwt jwt(String sub) {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", sub)
        );
    }

    private void authenticate(Jwt jwt) {
        AbstractAuthenticationToken auth = converter.convert(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void nonUuidSub_returns401() {
        Jwt badJwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", "no-es-un-uuid")
        );

        assertThatThrownBy(() -> converter.convert(badJwt))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void inactiveUser_returns403UserDeactivated() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(false, Role.TECHNICIAN, true)));

        assertThatThrownBy(() -> converter.convert(jwt(sub)))
                .isInstanceOf(AccountDesactivateException.class)
                .satisfies(ex -> assertThat(((AccountDesactivateException) ex).getReason())
                        .isEqualTo(AccountDesactivateException.AccountStatusReason.USER_DEACTIVATED));
    }

    @Test
    void inactiveOrganization_returns403OrganizationDeactivated() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(true, Role.TECHNICIAN, false)));

        assertThatThrownBy(() -> converter.convert(jwt(sub)))
                .isInstanceOf(AccountDesactivateException.class)
                .satisfies(ex -> assertThat(((AccountDesactivateException) ex).getReason())
                        .isEqualTo(AccountDesactivateException.AccountStatusReason.ORGANIZATION_DEACTIVATED));
    }

    @Test
    void platformAdmin_withInactiveOrganization_passes() throws Exception {
        String sub = UUID.randomUUID().toString();
        UUID id = UUID.fromString(sub);
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(true, Role.PLATFORM_ADMIN, false)));
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        authenticate(jwt(sub));
        UserDto dto = controller.me(jwt(sub));

        assertThat(dto.id()).isEqualTo(id);
    }

    @Test
    void inactivePlatformAdmin_returns403UserDeactivated() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(false, Role.PLATFORM_ADMIN, null)));

        assertThatThrownBy(() -> converter.convert(jwt(sub)))
                .isInstanceOf(AccountDesactivateException.class)
                .satisfies(ex -> assertThat(((AccountDesactivateException) ex).getReason())
                        .isEqualTo(AccountDesactivateException.AccountStatusReason.USER_DEACTIVATED));
    }

    @Test
    void activeUser_returns200() throws Exception {
        String sub = UUID.randomUUID().toString();
        UUID id = UUID.fromString(sub);
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(true, Role.CLIENT, true)));
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        authenticate(jwt(sub));
        UserDto dto = controller.me(jwt(sub));

        assertThat(dto.id()).isEqualTo(id);
    }

    @Test
    void userWithoutOnboarding_returns404() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.empty());

        authenticate(jwt(sub));

        assertThatThrownBy(() -> controller.me(jwt(sub)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
