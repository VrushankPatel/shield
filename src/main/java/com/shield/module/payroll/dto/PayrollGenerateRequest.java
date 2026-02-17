package com.shield.module.payroll.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record PayrollGenerateRequest(
        @NotNull UUID staffId,
        @Min(1) @Max(12) int month,
        @Min(2000) int year,
        @Min(1) int workingDays,
        BigDecimal totalDeductions,
        @Size(max = 50) String paymentMethod,
        @Size(max = 255) String paymentReference
) {
}
