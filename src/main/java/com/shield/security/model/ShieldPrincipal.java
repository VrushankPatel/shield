package com.shield.security.model;

import java.util.UUID;

public record ShieldPrincipal(
        UUID userId,
        UUID tenantId,
        String email,
        String role,
        String principalType,
        long tokenVersion
) {

    public ShieldPrincipal(UUID userId, UUID tenantId, String email, String role) {
        this(userId, tenantId, email, role, "USER", 0L);
    }
}
