package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LedgerEntryUpdateRequest(
        @NotNull LocalDate entryDate,
        UUID accountHeadId,
        UUID fundCategoryId,
        String transactionType,
        @NotNull @Positive BigDecimal amount,
        String type,
        String category,
        String reference,
        String referenceType,
        UUID referenceId,
        String description) {
}
