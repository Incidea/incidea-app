package com.incidea.incideabackend.common.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.incidea.incideabackend.common.exception.AccountDesactivateException;
import com.incidea.incideabackend.user.AccountStatus;
import com.incidea.incideabackend.user.Role;
import com.incidea.incideabackend.user.UserRepository;

/**
 * Converts the JWT into an {@link AbstractAuthenticationToken} and, in the same pass,
 * validates the account status with a single query ({@code LEFT JOIN} to
 * {@code Organization}).
 *
 * <p>If the user does not exist in {@code users} (onboarding not completed), the request
 * is allowed through. If the user is inactive, throws {@code USER_DEACTIVATED}. If the
 * organization is inactive, throws {@code ORGANIZATION_DEACTIVATED} unless the role is
 * {@code PLATFORM_ADMIN}, which only requires the user to be active.</p>
 */
public class JwtAuthenticationStatusConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
    private final UserRepository userRepository;

    public JwtAuthenticationStatusConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AbstractAuthenticationToken auth = delegate.convert(jwt);

        String sub = jwt.getClaimAsString("sub");
        UUID id;
        try {
            id = UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new InvalidBearerTokenException("Invalid subject claim: " + sub, e);
        }

        Optional<AccountStatus> status = userRepository.findAccountStatusById(id);
        if (status.isEmpty()) {
            return auth; // onboarding not completed, allow through
        }

        AccountStatus s = status.get();
        if (!s.active()) {
            throw new AccountDesactivateException(AccountDesactivateException.AccountStatusReason.USER_DEACTIVATED);
        }
        if (s.role() != Role.PLATFORM_ADMIN && Boolean.FALSE.equals(s.organizationActive())) {
            throw new AccountDesactivateException(AccountDesactivateException.AccountStatusReason.ORGANIZATION_DEACTIVATED);
        }

        return auth;
    }
}
