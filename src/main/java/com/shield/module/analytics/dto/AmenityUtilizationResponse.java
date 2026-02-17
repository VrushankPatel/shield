package com.shield.module.analytics.dto;

import java.util.UUID;

public record AmenityUtilizationResponse(
        UUID amenityId,
        String amenityName,
        long bookingCount
) {
}
