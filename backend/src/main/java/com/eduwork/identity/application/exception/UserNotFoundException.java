package com.eduwork.identity.application.exception;

import java.util.UUID;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super(String.format("User not found with ID: %s", userId));
    }

    public UserNotFoundException(String email) {
        super(String.format("User not found with email: %s", email));
    }
}
