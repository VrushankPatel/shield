package com.shield.module.amenities.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AmenityCancellationPolicyResponse(
        UUID id,
        UUID tenantId,
        UUID amenityId,
        Integer daysBeforeBooking,
        BigDecimal refundPercentage
) {
}
