package com.shield.module.amenities.dto;

import java.time.Instant;
import java.util.UUID;

public record AmenityAvailabilityResponse(
        UUID amenityId,
        Instant startTime,
        Instant endTime,
        boolean available,
        long conflictingBookings
) {
}
