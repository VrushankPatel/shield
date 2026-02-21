package com.shield.module.document.dto;

import com.shield.module.document.entity.DocumentAccessType;
import java.time.Instant;
import java.util.UUID;

public record DocumentAccessLogResponse(
        UUID id,
        UUID tenantId,
        UUID documentId,
        UUID accessedBy,
        DocumentAccessType accessType,
        Instant accessedAt
) {
}
