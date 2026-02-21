package com.shield.module.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record RootRefreshRequest(
        @NotBlank String refreshToken
) {
}
