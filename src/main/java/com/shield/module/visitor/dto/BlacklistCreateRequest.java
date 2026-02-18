package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record BlacklistCreateRequest(
        String personName,
        @NotBlank String phone,
        String reason,
        UUID blacklistedBy
) {
}
