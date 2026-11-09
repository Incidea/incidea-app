package com.incidea.incideabackend.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when the authenticated user or their organization is deactivated.
 * Translated to HTTP 403 by {@code AccountDesactivationEntryPoint}.
 */
public class AccountDesactivateException extends AuthenticationException {

    private final AccountStatusReason reason;

    public AccountDesactivateException(AccountStatusReason reason) {
        super(reason.getMessage());
        this.reason = reason;
    }

    public AccountStatusReason getReason() {
        return reason;
    }

    public enum AccountStatusReason {
        USER_DEACTIVATED("Usuario desactivado"),
        ORGANIZATION_DEACTIVATED("Organización desactivada");

        private final String message;

        AccountStatusReason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
