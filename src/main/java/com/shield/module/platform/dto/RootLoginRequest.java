package com.shield.module.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RootLoginRequest(
        @NotBlank @Size(max = 50) String loginId,
        @NotBlank @Size(min = 12, max = 128) String password
) {
}
