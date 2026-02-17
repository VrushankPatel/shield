package com.shield.security.model;

import java.util.UUID;

public record ShieldPrincipal(
        UUID userId,
        UUID tenantId,
        String email,
        String role
) {
}
