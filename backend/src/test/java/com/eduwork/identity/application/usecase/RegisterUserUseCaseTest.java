package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.RegisterUserCommand;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.domain.exception.EmailAlreadyExistsException;
import com.eduwork.identity.domain.exception.InvalidEmailException;
import com.eduwork.identity.domain.exception.InvalidPasswordException;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.model.UserRole;
import com.eduwork.identity.domain.model.UserStatus;
import com.eduwork.identity.domain.repository.EmailVerificationTokenRepository;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.domain.service.EmailService;
import com.eduwork.identity.domain.service.PasswordPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * TDD tests for RegisterUserUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase Application Service")
class RegisterUserUseCaseTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private EmailVerificationTokenRepository tokenRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private EmailService emailService;

        private PasswordPolicy passwordPolicy;
        private RegisterUserUseCase registerUserUseCase;

        @BeforeEach
        void setUp() {
                passwordPolicy = new PasswordPolicy();
                registerUserUseCase = new RegisterUserUseCase(
                                userRepository,
                                tokenRepository,
                                passwordEncoder,
                                passwordPolicy,
                                emailService);
        }

        @Test
        @DisplayName("Should register user successfully when valid command")
        void shouldRegisterUser_whenValidCommand() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("student@example.com")
                                .password("SecurePass123!")
                                .phone("+6281234567890")
                                .role(UserRole.STUDENT)
                                .build();

                when(userRepository.existsByEmail(command.getEmail())).thenReturn(false);
                when(userRepository.existsByPhone(command.getPhone())).thenReturn(false);
                when(passwordEncoder.encode(command.getPassword())).thenReturn("$2a$12$hashedPassword");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                UserResponseDTO result = registerUserUseCase.execute(command);

                // Then
                assertNotNull(result);
                assertEquals("student@example.com", result.getEmail());
                assertEquals("+6281234567890", result.getPhone());
                assertEquals(UserStatus.PENDING_VERIFICATION, result.getStatus());
                assertFalse(result.isProfileComplete());
                assertNotNull(result.getId());
                assertNotNull(result.getCreatedAt());

                // Verify interactions
                verify(userRepository).existsByEmail("student@example.com");
                verify(userRepository).existsByPhone("+6281234567890");
                verify(passwordEncoder).encode("SecurePass123!");
                verify(userRepository).save(any(User.class));
                verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowException_whenEmailExists() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("existing@example.com")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                when(userRepository.existsByEmail(command.getEmail())).thenReturn(true);

                // When & Then
                EmailAlreadyExistsException exception = assertThrows(
                                EmailAlreadyExistsException.class,
                                () -> registerUserUseCase.execute(command));

                assertTrue(exception.getMessage().contains("existing@example.com"));

                // Verify no save or email sent
                verify(userRepository, never()).save(any());
                verify(emailService, never()).sendVerificationEmail(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when phone already exists")
        void shouldThrowException_whenPhoneExists() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("new@example.com")
                                .password("SecurePass123!")
                                .phone("+6281234567890")
                                .role(UserRole.STUDENT)
                                .build();

                when(userRepository.existsByEmail(command.getEmail())).thenReturn(false);
                when(userRepository.existsByPhone(command.getPhone())).thenReturn(true);

                // When & Then
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> registerUserUseCase.execute(command));

                assertTrue(exception.getMessage().contains("Phone number already registered"));
                verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when password is weak")
        void shouldThrowException_whenPasswordWeak() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("student@example.com")
                                .password("weak")
                                .role(UserRole.STUDENT)
                                .build();

                // When & Then
                InvalidPasswordException exception = assertThrows(
                                InvalidPasswordException.class,
                                () -> registerUserUseCase.execute(command));

                // Verify no repository interactions
                verify(userRepository, never()).existsByEmail(any());
                verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should hash password before saving")
        void shouldHashPassword_beforeSaving() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("student@example.com")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                String hashedPassword = "$2a$12$hashedValueHere";
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(passwordEncoder.encode(command.getPassword())).thenReturn(hashedPassword);
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                registerUserUseCase.execute(command);

                // Then
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());

                User savedUser = userCaptor.getValue();
                assertEquals(hashedPassword, savedUser.getPasswordHash());
        }

        @Test
        @DisplayName("Should send verification email after registration")
        void shouldSendVerificationEmail_afterRegistration() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("student@example.com")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                registerUserUseCase.execute(command);

                // Then
                verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should register user without phone when phone is null")
        void shouldRegisterUser_whenPhoneIsNull() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("student@example.com")
                                .password("SecurePass123!")
                                .phone(null) // No phone
                                .role(UserRole.STUDENT)
                                .build();

                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                UserResponseDTO result = registerUserUseCase.execute(command);

                // Then
                assertNull(result.getPhone());
                verify(userRepository, never()).existsByPhone(any());
        }

        @Test
        @DisplayName("Should throw exception when email format is invalid")
        void shouldThrowException_whenEmailInvalid() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("invalid-email")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
                // Note: User domain constructor will throw InvalidEmailException
                // We don't need to mock save() - exception happens before that

                // When & Then - User constructor will throw InvalidEmailException
                assertThrows(
                                InvalidEmailException.class,
                                () -> registerUserUseCase.execute(command));

                // Verify save was never called (exception thrown before save)
                verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should normalize email to lowercase")
        void shouldNormalizeEmail_toLowerCase() {
                // Given
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email("Student@EXAMPLE.COM")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                UserResponseDTO result = registerUserUseCase.execute(command);

                // Then
                assertEquals("student@example.com", result.getEmail());
        }
}
