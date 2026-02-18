package com.shield.module.file.dto;

import java.time.Instant;

public record GeneratePresignedUrlResponse(
        String fileId,
        String uploadUrl,
        Instant expiresAt
) {
}
