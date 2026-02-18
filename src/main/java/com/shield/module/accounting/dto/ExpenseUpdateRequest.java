package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseUpdateRequest(
        @NotNull UUID accountHeadId,
        UUID fundCategoryId,
        UUID vendorId,
        @NotNull LocalDate expenseDate,
        @NotNull @Positive BigDecimal amount,
        String description,
        String invoiceNumber,
        String invoiceUrl,
        @NotNull String paymentStatus) {
}
