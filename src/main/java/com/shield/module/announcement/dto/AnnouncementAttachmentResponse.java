package com.shield.module.announcement.dto;

import java.time.Instant;
import java.util.UUID;

public record AnnouncementAttachmentResponse(
        UUID id,
        UUID tenantId,
        UUID announcementId,
        String fileName,
        String fileUrl,
        Long fileSize,
        String contentType,
        Instant createdAt) {
}
