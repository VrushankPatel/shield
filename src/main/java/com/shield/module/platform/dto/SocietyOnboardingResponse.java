package com.shield.module.platform.dto;

import java.util.UUID;

public record SocietyOnboardingResponse(
        UUID societyId,
        UUID adminUserId,
        String adminEmail
) {
}
