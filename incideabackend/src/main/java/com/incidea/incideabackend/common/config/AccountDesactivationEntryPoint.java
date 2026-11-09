package com.incidea.incideabackend.common.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.incidea.incideabackend.common.exception.AccountDesactivateException;

/**
 * {@link AuthenticationEntryPoint} for the OAuth2 resource server.
 *
 * <ul>
 *   <li>{@link AccountDesactivateException} → HTTP 403 with {@code ProblemDetail} and
 *       a {@code code} field ({@code USER_DEACTIVATED} or {@code ORGANIZATION_DEACTIVATED}).</li>
 *   <li>Any other {@link AuthenticationException} → HTTP 401 with
 *       {@code WWW-Authenticate: Bearer}.</li>
 * </ul>
 */
public class AccountDesactivationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        if (authException instanceof AccountDesactivateException ade) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            AccountDesactivateException.AccountStatusReason reason = ade.getReason();
            String json = """
                {"type":"https://tools.ietf.org/html/rfc7231#section-6.5.3",\
                 "title":"Forbidden",\
                 "status":%d,\
                 "detail":"%s",\
                 "code":"%s"}
                """.formatted(
                    HttpStatus.FORBIDDEN.value(),
                    reason.getMessage().replace("\"", "\\\""),
                    reason.name()
                );
            response.getWriter().write(json);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        String message = authException.getMessage() != null ? authException.getMessage() : "No autorizado";
        String json = """
            {"type":"https://tools.ietf.org/html/rfc7235#section-3.1",\
             "title":"Unauthorized",\
             "status":%d,\
             "detail":"%s"}
            """.formatted(HttpStatus.UNAUTHORIZED.value(), message.replace("\"", "\\\""));
        response.getWriter().write(json);
    }
}
