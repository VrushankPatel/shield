package com.shield.module.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RootChangePasswordRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^[0-9+ -]{7,20}$") String mobile,
        @NotBlank @Size(min = 12, max = 128) String newPassword,
        @NotBlank @Size(min = 12, max = 128) String confirmNewPassword
) {
}
