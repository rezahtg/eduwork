package com.eduwork.identity.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for completing student profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteStudentProfileRequest {

    @NotNull(message = "Grade level is required")
    @Min(value = 1, message = "Grade level must be between 1 and 12")
    @Max(value = 12, message = "Grade level must be between 1 and 12")
    private Integer gradeLevel;

    @NotBlank(message = "School name is required")
    @Size(max = 200, message = "School name must not exceed 200 characters")
    private String schoolName;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
