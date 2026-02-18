package com.shield.module.auth.dto;

import java.time.Instant;

public record LoginOtpSendResponse(
        String challengeToken,
        String destination,
        Instant expiresAt
) {
}
