package com.incidea.incideabackend.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import com.incidea.incideabackend.common.exception.AccountDesactivateException;

class AccountDesactivationEntryPointTest {

    @Test
    void deactivatedUser_responds403WithUserDeactivatedCode() throws Exception {
        AccountDesactivationEntryPoint entryPoint = new AccountDesactivationEntryPoint();
        AuthenticationException ex = new AccountDesactivateException(
                AccountDesactivateException.AccountStatusReason.USER_DEACTIVATED);

        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(null, response, ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getContentAsString()).contains("\"code\":\"USER_DEACTIVATED\"");
        assertThat(response.getContentAsString()).contains("\"detail\":\"Usuario desactivado\"");
    }

    @Test
    void deactivatedOrganization_responds403WithOrganizationDeactivatedCode() throws Exception {
        AccountDesactivationEntryPoint entryPoint = new AccountDesactivationEntryPoint();
        AuthenticationException ex = new AccountDesactivateException(
                AccountDesactivateException.AccountStatusReason.ORGANIZATION_DEACTIVATED);

        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(null, response, ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getContentAsString()).contains("\"code\":\"ORGANIZATION_DEACTIVATED\"");
        assertThat(response.getContentAsString()).contains("\"detail\":\"Organización desactivada\"");
    }

    @Test
    void otherAuthenticationException_responds401WithBearer() throws Exception {
        AccountDesactivationEntryPoint entryPoint = new AccountDesactivationEntryPoint();
        AuthenticationException ex = new BadCredentialsException("Credenciales inválidas");

        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(null, response, ex);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
    }
}
