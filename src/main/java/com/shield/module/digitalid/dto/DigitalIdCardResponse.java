package com.shield.module.digitalid.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DigitalIdCardResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        String qrCodeData,
        String qrCodeUrl,
        LocalDate issueDate,
        LocalDate expiryDate,
        boolean active,
        Instant deactivatedAt
) {
}
