package com.shield.module.document.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID tenantId,
        String documentName,
        UUID categoryId,
        String documentType,
        String fileUrl,
        Long fileSize,
        String description,
        String versionLabel,
        boolean publicAccess,
        UUID uploadedBy,
        Instant uploadDate,
        LocalDate expiryDate,
        String tags
) {
}
