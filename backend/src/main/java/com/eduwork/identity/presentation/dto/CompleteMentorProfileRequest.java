package com.eduwork.identity.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for completing mentor profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMentorProfileRequest {

    @NotBlank(message = "Bio is required")
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @NotBlank(message = "Qualifications are required")
    @Size(max = 500, message = "Qualifications must not exceed 500 characters")
    private String qualifications;

    @NotBlank(message = "Education is required")
    @Size(max = 500, message = "Education must not exceed 500 characters")
    private String education;

    @Size(max = 500, message = "Certifications must not exceed 500 characters")
    private String certifications;

    @Size(max = 200, message = "Portfolio website URL must not exceed 200 characters")
    private String portfolioWebsite;

    @Positive(message = "Hourly rate must be positive")
    private Long hourlyRateIdr;
}
