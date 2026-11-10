package com.incidea.incideabackend.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.incidea.incideabackend.common.exception.AccountDesactivateException;
import com.incidea.incideabackend.user.dto.AccountStatus;
import com.incidea.incideabackend.user.Role;
import com.incidea.incideabackend.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationStatusConverterTest {

    @Mock
    private UserRepository userRepository;

    private JwtAuthenticationStatusConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthenticationStatusConverter(userRepository);
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

    @Test
    void userNotFound_allowsOnboarding() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class))).thenReturn(Optional.empty());

        AbstractAuthenticationToken auth = converter.convert(jwt(sub));

        assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    void inactiveUser_throwsUserDeactivated() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(false, Role.TECHNICIAN, true)));

        assertThatThrownBy(() -> converter.convert(jwt(sub)))
                .isInstanceOf(AccountDesactivateException.class)
                .satisfies(ex -> assertThat(((AccountDesactivateException) ex).getReason())
                        .isEqualTo(AccountDesactivateException.AccountStatusReason.USER_DEACTIVATED));
    }

    @Test
    void inactiveOrganization_throwsOrganizationDeactivated() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(true, Role.TECHNICIAN, false)));

        assertThatThrownBy(() -> converter.convert(jwt(sub)))
                .isInstanceOf(AccountDesactivateException.class)
                .satisfies(ex -> assertThat(((AccountDesactivateException) ex).getReason())
                        .isEqualTo(AccountDesactivateException.AccountStatusReason.ORGANIZATION_DEACTIVATED));
    }

    @Test
    void platformAdmin_withInactiveOrganization_passes() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(true, Role.PLATFORM_ADMIN, false)));

        AbstractAuthenticationToken auth = converter.convert(jwt(sub));

        assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    void inactivePlatformAdmin_throwsUserDeactivated() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(false, Role.PLATFORM_ADMIN, null)));

        assertThatThrownBy(() -> converter.convert(jwt(sub)))
                .isInstanceOf(AccountDesactivateException.class)
                .satisfies(ex -> assertThat(((AccountDesactivateException) ex).getReason())
                        .isEqualTo(AccountDesactivateException.AccountStatusReason.USER_DEACTIVATED));
    }

    @Test
    void activeUserAndOrganization_returnsAuthentication() {
        String sub = UUID.randomUUID().toString();
        when(userRepository.findAccountStatusById(any(UUID.class)))
                .thenReturn(Optional.of(new AccountStatus(true, Role.CLIENT, true)));

        AbstractAuthenticationToken auth = converter.convert(jwt(sub));

        assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    void nonUuidSub_throwsInvalidBearerTokenException() {
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
}
