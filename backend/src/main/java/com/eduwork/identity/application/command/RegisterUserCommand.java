package com.eduwork.identity.application.command;

import com.eduwork.identity.domain.model.UserRole;
import lombok.Builder;
import lombok.Data;

/**
 * Command for user registration.
 * Represents the intent to register a new user.
 */
@Data
@Builder
public class RegisterUserCommand {

    private String email;
    private String password; // Raw password, will be hashed
    private String phone; // Optional
    private UserRole role; // STUDENT or MENTOR
}
