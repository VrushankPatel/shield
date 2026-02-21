package com.shield.module.amenities.dto;

import java.time.LocalTime;
import java.util.UUID;

public record AmenityTimeSlotResponse(
        UUID id,
        UUID tenantId,
        UUID amenityId,
        String slotName,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
}
