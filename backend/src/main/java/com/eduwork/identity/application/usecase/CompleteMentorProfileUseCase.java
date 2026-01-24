package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.CompleteMentorProfileCommand;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.domain.model.MentorProfile;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for completing mentor profile.
 * 
 * Business Rules:
 * - User must be authenticated
 * - Profile can be updated multiple times
 * - Sets profileComplete flag to true on first completion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteMentorProfileUseCase {

    private final UserRepository userRepository;

    /**
     * Completes or updates mentor profile for authenticated user.
     *
     * @param command profile data
     * @return updated user response
     * @throws IllegalStateException if user not found
     */
    @Transactional
    public UserResponseDTO execute(CompleteMentorProfileCommand command) {
        // Get authenticated user ID from security context
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(userIdStr);

        log.info("Completing mentor profile for user: {}", userId);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Create mentor profile value object
        MentorProfile profile = MentorProfile.create(
                command.getBio(),
                command.getQualifications(),
                command.getEducation(),
                command.getCertifications(),
                command.getPortfolioWebsite(),
                command.getHourlyRateIdr());

        // Update or complete profile
        if (user.isProfileComplete()) {
            user.updateMentorProfile(profile);
            log.info("Updated mentor profile for user: {}", userId);
        } else {
            user.completeMentorProfile(profile);
            log.info("Completed mentor profile for user: {}", userId);
        }

        // Save user
        user = userRepository.save(user);

        // Map to response DTO
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .profileComplete(user.isProfileComplete())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
