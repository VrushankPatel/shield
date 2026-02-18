package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceBulkGenerateRequest(
        @NotEmpty List<@NotNull UUID> unitIds,
        UUID billingCycleId,
        @NotNull LocalDate dueDate,
        @NotNull @PositiveOrZero BigDecimal subtotal,
        @NotNull @PositiveOrZero BigDecimal lateFee,
        @NotNull @PositiveOrZero BigDecimal gstAmount,
        @NotNull @PositiveOrZero BigDecimal otherCharges) {
}
