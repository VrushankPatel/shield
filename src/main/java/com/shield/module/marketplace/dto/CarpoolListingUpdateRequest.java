package com.shield.module.marketplace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record CarpoolListingUpdateRequest(
        @NotBlank @Size(max = 255) String routeFrom,
        @NotBlank @Size(max = 255) String routeTo,
        @NotNull LocalTime departureTime,
        @NotNull @Min(1) Integer availableSeats,
        @NotBlank @Size(max = 100) String daysOfWeek,
        @Size(max = 50) String vehicleType,
        @Size(max = 50) String contactPreference,
        Boolean active
) {
}
