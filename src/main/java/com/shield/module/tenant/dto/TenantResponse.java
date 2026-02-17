package com.shield.module.tenant.dto;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String address,
        Instant createdAt,
        Instant updatedAt
) {
}
