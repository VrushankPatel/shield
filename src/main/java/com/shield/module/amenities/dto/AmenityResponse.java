package com.shield.module.amenities.dto;

import java.util.UUID;

public record AmenityResponse(
        UUID id,
        UUID tenantId,
        String name,
        Integer capacity,
        boolean requiresApproval
) {
}
