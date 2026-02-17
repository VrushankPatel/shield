package com.shield.module.user.dto;

import com.shield.module.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UserCreateRequest(
        UUID unitId,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @Pattern(regexp = "^[0-9+ -]{7,20}$") String phone,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull UserRole role
) {
}
