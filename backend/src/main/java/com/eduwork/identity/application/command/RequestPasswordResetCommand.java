package com.eduwork.identity.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for requesting password reset.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordResetCommand {
    private String email;
}
