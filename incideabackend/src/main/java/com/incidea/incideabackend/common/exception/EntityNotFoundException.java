package com.incidea.incideabackend.common.exception;

/**
 * Exception thrown when a requested entity (for example, a user, organization or incident)
 * is not found and should be translated to an HTTP 404 Not Found response.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
