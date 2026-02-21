package com.shield.security.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shield.common.exception.BadRequestException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PasswordPolicyServiceTest {

    private PasswordPolicyService passwordPolicyService;

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyService();
        ReflectionTestUtils.setField(passwordPolicyService, "minLength", 12);
        ReflectionTestUtils.setField(passwordPolicyService, "maxLength", 128);
        ReflectionTestUtils.setField(passwordPolicyService, "requireUpper", true);
        ReflectionTestUtils.setField(passwordPolicyService, "requireLower", true);
        ReflectionTestUtils.setField(passwordPolicyService, "requireDigit", true);
        ReflectionTestUtils.setField(passwordPolicyService, "requireSpecial", true);
    }

    @Test
    void validateShouldReturnNoViolationsForStrongPassword() {
        List<String> violations = passwordPolicyService.validate("StrongPassword#123");
        assertTrue(violations.isEmpty());
    }

    @Test
    void validateShouldReturnViolationsForWeakPassword() {
        List<String> violations = passwordPolicyService.validate("weak");
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("at least 12 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.contains("uppercase")));
        assertTrue(violations.stream().anyMatch(v -> v.contains("digit")));
        assertTrue(violations.stream().anyMatch(v -> v.contains("special character")));
    }

    @Test
    void validateOrThrowShouldThrowForBlankPassword() {
        assertThrows(BadRequestException.class, () -> passwordPolicyService.validateOrThrow(" ", "Password"));
    }

    @Test
    void validateOrThrowShouldPassForCompliantPassword() {
        assertDoesNotThrow(() -> passwordPolicyService.validateOrThrow("Compliant#123A", "Password"));
    }
}
