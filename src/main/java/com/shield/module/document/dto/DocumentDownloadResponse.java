package com.shield.module.document.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentDownloadResponse(
        UUID documentId,
        String documentName,
        String fileUrl,
        Instant downloadedAt
) {
}
