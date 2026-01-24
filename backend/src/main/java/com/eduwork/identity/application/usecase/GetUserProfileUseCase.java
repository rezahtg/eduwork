package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.exception.UserNotFoundException;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.presentation.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Get user profile information
 * 
 * Simple read operation - returns user data with profile.
 * Performance: < 20ms (cached at repository level when enabled)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserProfileUseCase {

    private final UserRepository userRepository;

    /**
     * Execute use case to get user profile.
     * 
     * @param userId User ID from authentication token
     * @return User profile with student or mentor data
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserProfileResponse execute(UUID userId) {
        log.debug("Fetching profile for user: {}", userId);

        // Simple lookup - will be cached when caching re-enabled
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        log.debug("Profile retrieved for user: {}", userId);
        return UserProfileResponse.from(user);
    }
}
