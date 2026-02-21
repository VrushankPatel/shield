package com.shield.module.announcement.dto;

import java.time.Instant;
import java.util.UUID;

public record AnnouncementReadReceiptResponse(
        UUID id,
        UUID announcementId,
        UUID userId,
        Instant readAt,
        Instant createdAt
) {
}
