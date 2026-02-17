package com.shield.module.utility.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WaterLevelLogCreateRequest(
        @NotNull UUID tankId,
        Instant readingTime,
        @NotNull @DecimalMin("0.0") BigDecimal levelPercentage,
        BigDecimal volume
) {
}
