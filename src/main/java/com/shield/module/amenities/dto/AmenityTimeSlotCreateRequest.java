package com.shield.module.amenities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record AmenityTimeSlotCreateRequest(
        @NotBlank String slotName,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        Boolean active
) {
}
