package com.shield.module.utility.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ElectricityReadingCreateRequest(
        @NotNull UUID meterId,
        @NotNull LocalDate readingDate,
        @NotNull @DecimalMin("0.0") BigDecimal readingValue,
        BigDecimal unitsConsumed,
        BigDecimal cost
) {
}
