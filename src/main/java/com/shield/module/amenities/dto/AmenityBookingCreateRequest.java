package com.shield.module.amenities.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record AmenityBookingCreateRequest(
        @NotNull UUID unitId,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        String notes
) {
}
