package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotBlank;

public record BlacklistUpdateRequest(
        String personName,
        @NotBlank String phone,
        String reason
) {
}
