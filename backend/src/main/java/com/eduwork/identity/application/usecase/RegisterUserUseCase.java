package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.RegisterUserCommand;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.application.mapper.UserMapper;
import com.eduwork.identity.domain.exception.EmailAlreadyExistsException;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.domain.service.EmailService;
import com.eduwork.identity.domain.service.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for user registration.
 * Orchestrates the registration flow following business rules.
 * 
 * Flow:
 * 1. Validate password policy
 * 2. Check email uniqueness
 * 3. Check phone uniqueness (if provided)
 * 4. Hash password
 * 5. Create user
 * 6. Send verification email (async)
 * 7. Return user response
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final EmailService emailService;

    /**
     * Registers a new user.
     * 
     * @param command registration command
     * @return user response DTO
     * @throws EmailAlreadyExistsException                                    if
     *                                                                        email
     *                                                                        already
     *                                                                        registered
     * @throws com.eduwork.identity.domain.exception.InvalidPasswordException if
     *                                                                        password
     *                                                                        weak
     * @throws com.eduwork.identity.domain.exception.InvalidEmailException    if
     *                                                                        email
     *                                                                        invalid
     */
    @Transactional
    public UserResponseDTO execute(RegisterUserCommand command) {
        log.info("Registering new user: email={}, role={}", command.getEmail(), command.getRole());

        // 1. Validate password policy
        passwordPolicy.validate(command.getPassword());

        // 2. Check email uniqueness
        if (userRepository.existsByEmail(command.getEmail())) {
            log.warn("Registration failed: email already exists: {}", command.getEmail());
            throw new EmailAlreadyExistsException(command.getEmail());
        }

        // 3. Check phone uniqueness (if provided)
        if (command.getPhone() != null && userRepository.existsByPhone(command.getPhone())) {
            log.warn("Registration failed: phone already exists: {}", command.getPhone());
            throw new RuntimeException("Phone number already registered");
        }

        // 4. Hash password (BCrypt strength 12)
        String passwordHash = passwordEncoder.encode(command.getPassword());

        // 5. Create user (domain validates email)
        User user = new User(
                command.getEmail(),
                passwordHash,
                command.getPhone());

        // 6. Save user
        User savedUser = userRepository.save(user);

        // 7. Send verification email asynchronously
        String verificationToken = generateVerificationToken();
        emailService.sendVerificationEmail(savedUser, verificationToken);

        log.info("User registered successfully: userId={}, email={}",
                savedUser.getId(), savedUser.getEmail());

        // 8. Return response DTO
        return UserMapper.toResponseDTO(savedUser);
    }

    /**
     * Generates secure verification token.
     * TODO: Use proper token service with Redis storage
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
}
