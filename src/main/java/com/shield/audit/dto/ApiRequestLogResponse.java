package com.shield.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record ApiRequestLogResponse(
        UUID id,
        String requestId,
        UUID tenantId,
        UUID userId,
        String endpoint,
        String httpMethod,
        Integer responseStatus,
        Long responseTimeMs,
        String ipAddress,
        String userAgent,
        Instant createdAt
) {
}
