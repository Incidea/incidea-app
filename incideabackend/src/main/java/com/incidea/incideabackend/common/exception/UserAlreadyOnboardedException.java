package com.incidea.incideabackend.common.exception;

public class UserAlreadyOnboardedException extends RuntimeException {

    public UserAlreadyOnboardedException(String message) {
        super(message);
    }
}
