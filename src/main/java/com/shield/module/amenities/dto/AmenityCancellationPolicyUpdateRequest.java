package com.shield.module.amenities.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AmenityCancellationPolicyUpdateRequest(
        @NotNull @Min(0) Integer daysBeforeBooking,
        @NotNull @Min(0) @Max(100) BigDecimal refundPercentage
) {
}
