package com.shield.module.amenities.dto;

import com.shield.module.amenities.entity.AmenityBookingStatus;
import java.time.Instant;
import java.util.UUID;

public record AmenityBookingResponse(
        UUID id,
        UUID tenantId,
        UUID amenityId,
        UUID unitId,
        Instant startTime,
        Instant endTime,
        AmenityBookingStatus status,
        String notes
) {
}
