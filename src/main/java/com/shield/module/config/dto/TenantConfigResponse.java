package com.shield.module.config.dto;

import java.time.Instant;
import java.util.UUID;

public record TenantConfigResponse(
        UUID id,
        UUID tenantId,
        String key,
        String value,
        String category,
        Instant createdAt,
        Instant updatedAt
) {
}
