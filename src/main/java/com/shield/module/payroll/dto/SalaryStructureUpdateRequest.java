package com.shield.module.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryStructureUpdateRequest(
        @NotNull UUID payrollComponentId,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        boolean active,
        @NotNull LocalDate effectiveFrom
) {
}
