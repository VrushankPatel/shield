package com.shield.module.notification.dto;

import com.shield.module.notification.entity.NotificationDeliveryStatus;
import java.time.Instant;
import java.util.UUID;

public record NotificationLogResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        String recipientEmail,
        String subject,
        String body,
        NotificationDeliveryStatus status,
        String errorMessage,
        String sourceType,
        UUID sourceId,
        Instant sentAt,
        Instant createdAt
) {
}
