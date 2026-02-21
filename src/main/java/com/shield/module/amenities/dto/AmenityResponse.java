package com.shield.module.amenities.dto;

import java.util.UUID;

public record AmenityResponse(
        UUID id,
        UUID tenantId,
        String name,
        String amenityType,
        String description,
        Integer capacity,
        String location,
        boolean bookingAllowed,
        Integer advanceBookingDays,
        boolean requiresApproval,
        boolean active
) {
}
