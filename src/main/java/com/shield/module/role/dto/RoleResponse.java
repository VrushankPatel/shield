package com.shield.module.role.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        UUID tenantId,
        String code,
        String name,
        String description,
        boolean systemRole,
        Instant createdAt,
        Instant updatedAt
) {
}
