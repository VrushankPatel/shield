package com.shield.module.billing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BillingCycleUpdateRequest(
        @NotBlank String cycleName,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @Min(2000) @Max(2200) Integer year,
        @NotNull LocalDate dueDate,
        LocalDate lateFeeApplicableDate) {
}
