package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.exception.UserNotFoundException;
import com.eduwork.identity.domain.model.*;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.presentation.dto.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserProfileUseCase")
class GetUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserProfileUseCase useCase;

    private UUID userId;
    private User studentUser;
    private User mentorUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Student user
        studentUser = new User(
                userId,
                "student@example.com",
                "+6281234567890",
                "hashedPassword",
                UserStatus.ACTIVE,
                true,
                Instant.now(),
                null,
                null,
                StudentProfile.create(12, "SMA Negeri 1", "Student bio"),
                null,
                Instant.now(),
                Instant.now());

        // Mentor user
        mentorUser = new User(
                UUID.randomUUID(),
                "mentor@example.com",
                "+6281234567891",
                "hashedPassword",
                UserStatus.ACTIVE,
                true,
                Instant.now(),
                null,
                null,
                null,
                MentorProfile.create(
                        "Mentor bio",
                        "Math expert with 10 years experience",
                        "S1 Mathematics from UI",
                        "Certified Teacher",
                        "https://portfolio.com",
                        150000L),
                Instant.now(),
                Instant.now());
    }

    @Test
    @DisplayName("Should return student profile successfully")
    void shouldReturnStudentProfile() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(studentUser));

        // When
        UserProfileResponse result = useCase.execute(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("student@example.com");
        assertThat(result.isProfileComplete()).isTrue();
        assertThat(result.getStudentProfile()).isNotNull();
        assertThat(result.getStudentProfile().getGradeLevel()).isEqualTo(12);
        assertThat(result.getStudentProfile().getSchoolName()).isEqualTo("SMA Negeri 1");
        assertThat(result.getMentorProfile()).isNull();

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should return mentor profile successfully")
    void shouldReturnMentorProfile() {
        // Given
        UUID mentorId = mentorUser.getId();
        when(userRepository.findById(mentorId)).thenReturn(Optional.of(mentorUser));

        // When
        UserProfileResponse result = useCase.execute(mentorId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("mentor@example.com");
        assertThat(result.getMentorProfile()).isNotNull();
        assertThat(result.getMentorProfile().getHourlyRateIdr()).isEqualTo(150000L);
        assertThat(result.getMentorProfile().getBio()).isEqualTo("Mentor bio");
        assertThat(result.getStudentProfile()).isNull();
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
    }
}
