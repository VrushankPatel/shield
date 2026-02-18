package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SpecialAssessmentUpdateRequest(
        @NotBlank String assessmentName,
        String description,
        @NotNull @Positive BigDecimal totalAmount,
        BigDecimal perUnitAmount,
        LocalDate assessmentDate,
        @NotNull LocalDate dueDate,
        @NotBlank String status) {
}
