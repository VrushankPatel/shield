package com.shield.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record SystemLogResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        String logLevel,
        String loggerName,
        String message,
        String exceptionTrace,
        String endpoint,
        String correlationId,
        Instant createdAt
) {
}
