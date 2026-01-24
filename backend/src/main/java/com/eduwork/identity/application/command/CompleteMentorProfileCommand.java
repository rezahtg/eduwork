package com.eduwork.identity.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for completing mentor profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMentorProfileCommand {
    private String bio;
    private String qualifications;
    private String education;
    private String certifications;
    private String portfolioWebsite;
    private Long hourlyRateIdr;
}
