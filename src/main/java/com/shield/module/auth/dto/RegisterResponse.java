package com.shield.module.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        UUID tenantId,
        String email,
        boolean emailVerificationRequired
) {
}
