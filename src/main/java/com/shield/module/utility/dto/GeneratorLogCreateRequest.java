package com.shield.module.utility.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GeneratorLogCreateRequest(
        @NotNull UUID generatorId,
        @NotNull LocalDate logDate,
        Instant startTime,
        Instant stopTime,
        @DecimalMin("0.0") BigDecimal runtimeHours,
        @DecimalMin("0.0") BigDecimal dieselConsumed,
        @DecimalMin("0.0") BigDecimal dieselCost,
        @DecimalMin("0.0") BigDecimal meterReadingBefore,
        @DecimalMin("0.0") BigDecimal meterReadingAfter,
        @DecimalMin("0.0") BigDecimal unitsGenerated,
        UUID operatorId
) {
}
