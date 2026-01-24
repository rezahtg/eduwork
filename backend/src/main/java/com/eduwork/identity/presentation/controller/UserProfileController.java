package com.eduwork.identity.presentation.controller;

import com.eduwork.common.dto.ApiResponse;
import com.eduwork.identity.application.command.CompleteMentorProfileCommand;
import com.eduwork.identity.application.command.CompleteStudentProfileCommand;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.application.usecase.CompleteMentorProfileUseCase;
import com.eduwork.identity.application.usecase.CompleteStudentProfileUseCase;
import com.eduwork.identity.presentation.dto.CompleteMentorProfileRequest;
import com.eduwork.identity.presentation.dto.CompleteStudentProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 * Handles profile completion for students and mentors.
 */
@RestController
@RequestMapping("/users/me/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final CompleteStudentProfileUseCase completeStudentProfileUseCase;
    private final CompleteMentorProfileUseCase completeMentorProfileUseCase;

    /**
     * Complete or update student profile.
     * Requires JWT authentication.
     *
     * @param request student profile data
     * @return updated user response
     */
    @PutMapping("/student")
    public ResponseEntity<ApiResponse<UserResponseDTO>> completeStudentProfile(
            @Valid @RequestBody CompleteStudentProfileRequest request) {

        log.info("Completing student profile");

        // Map request to command
        CompleteStudentProfileCommand command = CompleteStudentProfileCommand.builder()
                .gradeLevel(request.getGradeLevel())
                .schoolName(request.getSchoolName())
                .bio(request.getBio())
                .build();

        // Execute use case
        UserResponseDTO user = completeStudentProfileUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        user,
                        "Student profile completed successfully"));
    }

    /**
     * Complete or update mentor profile.
     * Requires JWT authentication.
     *
     * @param request mentor profile data
     * @return updated user response
     */
    @PutMapping("/mentor")
    public ResponseEntity<ApiResponse<UserResponseDTO>> completeMentorProfile(
            @Valid @RequestBody CompleteMentorProfileRequest request) {

        log.info("Completing mentor profile");

        // Map request to command
        CompleteMentorProfileCommand command = CompleteMentorProfileCommand.builder()
                .bio(request.getBio())
                .qualifications(request.getQualifications())
                .education(request.getEducation())
                .certifications(request.getCertifications())
                .portfolioWebsite(request.getPortfolioWebsite())
                .hourlyRateIdr(request.getHourlyRateIdr())
                .build();

        // Execute use case
        UserResponseDTO user = completeMentorProfileUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        user,
                        "Mentor profile completed successfully"));
    }
}
