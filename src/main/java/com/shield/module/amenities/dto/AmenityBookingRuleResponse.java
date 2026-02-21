package com.shield.module.amenities.dto;

import java.util.UUID;

public record AmenityBookingRuleResponse(
        UUID id,
        UUID tenantId,
        UUID amenityId,
        String ruleType,
        String ruleValue,
        boolean active
) {
}
