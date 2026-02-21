package com.shield.security.jwt;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretStartupValidator {

    private static final String DEFAULT_SECRET = "change-me-change-me-change-me-change-me";

    private final Environment environment;
    private final String jwtSecret;

    public JwtSecretStartupValidator(
            Environment environment,
            @Value("${shield.security.jwt.secret}") String jwtSecret) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
    }

    @PostConstruct
    void validate() {
        if (isProdProfileActive() && DEFAULT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("JWT secret must be overridden in production profile");
        }
    }

    private boolean isProdProfileActive() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
