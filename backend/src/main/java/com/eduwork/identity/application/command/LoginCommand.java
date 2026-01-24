package com.eduwork.identity.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for user login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {
    private String email;
    private String password;
    private String deviceInfo; // User agent / device information for session tracking
}
