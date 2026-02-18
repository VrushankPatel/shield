package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceUpdateRequest(
        @NotNull LocalDate dueDate,
        @NotNull @PositiveOrZero BigDecimal subtotal,
        @NotNull @PositiveOrZero BigDecimal lateFee,
        @NotNull @PositiveOrZero BigDecimal gstAmount,
        @NotNull @PositiveOrZero BigDecimal otherCharges,
        @NotNull @PositiveOrZero BigDecimal outstandingAmount,
        @NotNull String status) {
}
