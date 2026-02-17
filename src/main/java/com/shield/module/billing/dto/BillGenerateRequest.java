package com.shield.module.billing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BillGenerateRequest(
        @NotNull UUID unitId,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @Min(2000) @Max(2200) Integer year,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate dueDate,
        BigDecimal lateFee
) {
}
