package com.shield.module.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        boolean emailEnabled,
        Instant createdAt,
        Instant updatedAt
) {
}
