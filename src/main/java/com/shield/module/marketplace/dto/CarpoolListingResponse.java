package com.shield.module.marketplace.dto;

import java.time.LocalTime;
import java.util.UUID;

public record CarpoolListingResponse(
        UUID id,
        UUID tenantId,
        UUID postedBy,
        String routeFrom,
        String routeTo,
        LocalTime departureTime,
        Integer availableSeats,
        String daysOfWeek,
        String vehicleType,
        String contactPreference,
        boolean active
) {
}
