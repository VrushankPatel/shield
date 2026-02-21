package com.shield.module.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SocietyOnboardingRequest(
        @NotBlank @Size(max = 200) String societyName,
        @Size(max = 500) String societyAddress,
        @NotBlank @Size(max = 200) String adminName,
        @NotBlank @Email @Size(max = 255) String adminEmail,
        @Pattern(regexp = "^[0-9+ -]{7,20}$") String adminPhone,
        @NotBlank @Size(min = 12, max = 128) String adminPassword
) {
}
