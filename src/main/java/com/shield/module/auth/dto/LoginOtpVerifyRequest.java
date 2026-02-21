package com.shield.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginOtpVerifyRequest(
        @NotBlank @Size(max = 128) String challengeToken,
        @NotBlank @Pattern(regexp = "^\\d{4,8}$") String otpCode
) {
}
