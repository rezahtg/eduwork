package com.eduwork.identity.domain.service;

import com.eduwork.identity.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for PasswordPolicy domain service.
 */
@DisplayName("PasswordPolicy Domain Service")
class PasswordPolicyTest {

    private PasswordPolicy passwordPolicy;

    @BeforeEach
    void setUp() {
        passwordPolicy = new PasswordPolicy();
    }

    @Test
    @DisplayName("Should validate password when meets all requirements")
    void shouldValidate_whenPasswordMeetsAllRequirements() {
        // Given
        String validPassword = "SecurePass123!";

        // When & Then
        assertDoesNotThrow(() -> passwordPolicy.validate(validPassword));
        assertTrue(passwordPolicy.isValid(validPassword));
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void shouldThrowException_whenPasswordIsNull() {
        // When & Then
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(null));
        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password is empty")
    void shouldThrowException_whenPasswordIsEmpty() {
        // When & Then
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(""));
        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password too short")
    void shouldThrowException_whenPasswordTooShort() {
        // Given
        String shortPassword = "Pass1!"; // Only 6 characters

        // When & Then
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(shortPassword));
        assertTrue(exception.getMessage().contains("at least 8 characters"));
    }

    @Test
    @DisplayName("Should throw exception when no uppercase letter")
    void shouldThrowException_whenNoUppercase() {
        // Given
        String noUppercase = "password123!";

        // When & Then
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(noUppercase));
        assertTrue(exception.getMessage().contains("uppercase letter"));
    }

    @Test
    @DisplayName("Should throw exception when no lowercase letter")
    void shouldThrowException_whenNoLowercase() {
        // Given
        String noLowercase = "PASSWORD123!";

        // When & Then
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(noLowercase));
        assertTrue(exception.getMessage().contains("lowercase letter"));
    }

    @Test
    @DisplayName("Should throw exception when no digit")
    void shouldThrowException_whenNoDigit() {
        // Given
        String noDigit = "Password!";

        // When & Then
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(noDigit));
        assertTrue(exception.getMessage().contains("digit"));
    }

    @Test
    @DisplayName("Should throw exception when no special character")
    void shouldThrowException_whenNoSpecialChar() {
        // Given
        String noSpecial = "Password123";

        // When & Then
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(noSpecial));
        assertTrue(exception.getMessage().contains("special character"));
    }

    @Test
    @DisplayName("Should validate password with all allowed special characters")
    void shouldValidate_withAllowedSpecialCharacters() {
        // Given
        String[] validPasswords = {
                "Password123@",
                "Password123$",
                "Password123!",
                "Password123%",
                "Password123*",
                "Password123?",
                "Password123&"
        };

        // When & Then
        for (String password : validPasswords) {
            assertDoesNotThrow(() -> passwordPolicy.validate(password),
                    "Should validate password with: " + password);
        }
    }

    @Test
    @DisplayName("Should return false for invalid password in isValid method")
    void shouldReturnFalse_whenPasswordInvalid() {
        // Given
        String invalidPassword = "weak";

        // When
        boolean valid = passwordPolicy.isValid(invalidPassword);

        // Then
        assertFalse(valid);
    }

    @Test
    @DisplayName("Should validate exact 8 character password")
    void shouldValidate_whenExactly8Characters() {
        // Given
        String password = "Pass123!"; // Exactly 8 chars

        // When & Then
        assertDoesNotThrow(() -> passwordPolicy.validate(password));
    }
}
