package com.shield.module.role.dto;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        UUID tenantId,
        String code,
        String moduleName,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
