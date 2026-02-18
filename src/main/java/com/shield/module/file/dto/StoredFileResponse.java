package com.shield.module.file.dto;

import com.shield.module.file.entity.StoredFileStatus;
import java.time.Instant;
import java.util.UUID;

public record StoredFileResponse(
        UUID id,
        UUID tenantId,
        String fileId,
        String fileName,
        String contentType,
        long fileSize,
        String checksum,
        StoredFileStatus status,
        UUID uploadedBy,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {
}
