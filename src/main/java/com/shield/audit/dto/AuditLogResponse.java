package com.shield.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        String action,
        String entityType,
        UUID entityId,
        String payload,
        Instant createdAt
) {
}
