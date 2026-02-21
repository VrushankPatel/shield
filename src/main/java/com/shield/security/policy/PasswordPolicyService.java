package com.shield.security.policy;

import com.shield.common.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    @Value("${shield.security.password.min-length:12}")
    private int minLength;

    @Value("${shield.security.password.max-length:128}")
    private int maxLength;

    @Value("${shield.security.password.require-upper:true}")
    private boolean requireUpper;

    @Value("${shield.security.password.require-lower:true}")
    private boolean requireLower;

    @Value("${shield.security.password.require-digit:true}")
    private boolean requireDigit;

    @Value("${shield.security.password.require-special:true}")
    private boolean requireSpecial;

    public void validateOrThrow(String password, String fieldLabel) {
        List<String> violations = validate(password);
        if (!violations.isEmpty()) {
            String label = (fieldLabel == null || fieldLabel.isBlank()) ? "Password" : fieldLabel;
            throw new BadRequestException(label + " does not meet security policy: " + String.join("; ", violations));
        }
    }

    public List<String> validate(String password) {
        List<String> violations = new ArrayList<>();
        if (password == null || password.isBlank()) {
            violations.add("must not be blank");
            return violations;
        }

        if (password.length() < minLength) {
            violations.add("must be at least " + minLength + " characters");
        }
        if (password.length() > maxLength) {
            violations.add("must be at most " + maxLength + " characters");
        }
        if (requireUpper && password.chars().noneMatch(Character::isUpperCase)) {
            violations.add("must include at least one uppercase letter");
        }
        if (requireLower && password.chars().noneMatch(Character::isLowerCase)) {
            violations.add("must include at least one lowercase letter");
        }
        if (requireDigit && password.chars().noneMatch(Character::isDigit)) {
            violations.add("must include at least one digit");
        }
        if (requireSpecial && password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
            violations.add("must include at least one special character");
        }

        return violations;
    }
}
