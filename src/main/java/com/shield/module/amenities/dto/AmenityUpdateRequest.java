package com.shield.module.amenities.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AmenityUpdateRequest(
        @NotBlank String name,
        String amenityType,
        String description,
        @NotNull @Min(1) Integer capacity,
        String location,
        Boolean bookingAllowed,
        @Min(1) Integer advanceBookingDays,
        Boolean active,
        boolean requiresApproval
) {
}
