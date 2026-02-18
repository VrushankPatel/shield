package com.shield.module.digitalid.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DigitalIdVerificationResponse(
        UUID cardId,
        UUID userId,
        String qrCodeData,
        boolean active,
        boolean expired,
        boolean valid,
        LocalDate expiryDate,
        String message
) {
}
