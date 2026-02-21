package com.shield.module.amenities.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AmenityPricingResponse(
        UUID id,
        UUID tenantId,
        UUID amenityId,
        UUID timeSlotId,
        String dayType,
        BigDecimal basePrice,
        boolean peakHour,
        BigDecimal peakHourMultiplier
) {
}
