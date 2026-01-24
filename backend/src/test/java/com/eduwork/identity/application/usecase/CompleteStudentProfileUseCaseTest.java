package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.CompleteStudentProfileCommand;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.domain.model.StudentProfile;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.model.UserStatus;
import com.eduwork.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteStudentProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CompleteStudentProfileUseCase useCase;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User(
                "student@example.com",
                "hashedPassword123",
                null);

        // Set up security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldCompleteStudentProfile() {
        // Given
        CompleteStudentProfileCommand command = CompleteStudentProfileCommand.builder()
                .gradeLevel(10)
                .schoolName("Jakarta International School")
                .bio("I love learning mathematics")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponseDTO result = useCase.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isProfileComplete()).isTrue();
        verify(userRepository).save(any(User.class));
    }
}
