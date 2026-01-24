package com.eduwork.identity.presentation.controller;

import com.eduwork.common.dto.ApiResponse;
import com.eduwork.identity.application.usecase.GetUserProfileUseCase;
import com.eduwork.identity.presentation.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for user profile operations.
 * Handles profile retrieval and related endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Get current user's profile.
     * 
     * @param userId User ID from JWT token (auto-injected by Spring Security)
     * @return User profile with student or mentor data
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal String userId) {

        log.info("Profile request for user: {}", userId);

        UserProfileResponse profile = getUserProfileUseCase.execute(UUID.fromString(userId));

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully"));
    }
}
