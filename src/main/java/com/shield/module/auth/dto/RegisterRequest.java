package com.shield.module.auth.dto;

import com.shield.module.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegisterRequest(
        @NotNull UUID tenantId,
        UUID unitId,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 32) String phone,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotNull UserRole role
) {
}
