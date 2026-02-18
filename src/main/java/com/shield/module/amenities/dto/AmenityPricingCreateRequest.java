package com.shield.module.amenities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AmenityPricingCreateRequest(
        @NotNull UUID timeSlotId,
        @NotBlank String dayType,
        @NotNull BigDecimal basePrice,
        Boolean peakHour,
        BigDecimal peakHourMultiplier
) {
}
