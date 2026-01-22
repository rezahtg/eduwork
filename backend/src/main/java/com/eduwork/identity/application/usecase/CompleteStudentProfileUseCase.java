package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.CompleteStudentProfileCommand;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.domain.model.StudentProfile;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for completing student profile.
 * 
 * Business Rules:
 * - User must be authenticated
 * - Profile can be updated multiple times
 * - Sets profileComplete flag to true on first completion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteStudentProfileUseCase {

    private final UserRepository userRepository;

    /**
     * Completes or updates student profile for authenticated user.
     *
     * @param command profile data
     * @return updated user response
     * @throws IllegalStateException if user not found
     */
    @Transactional
    public UserResponseDTO execute(CompleteStudentProfileCommand command) {
        // Get authenticated user ID from security context
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(userIdStr);

        log.info("Completing student profile for user: {}", userId);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Create student profile value object
        StudentProfile profile = StudentProfile.create(
                command.getGradeLevel(),
                command.getSchoolName(),
                command.getBio());

        // Update or complete profile
        if (user.isProfileComplete()) {
            user.updateStudentProfile(profile);
            log.info("Updated student profile for user: {}", userId);
        } else {
            user.completeStudentProfile(profile);
            log.info("Completed student profile for user: {}", userId);
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
