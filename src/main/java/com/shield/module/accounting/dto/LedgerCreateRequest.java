package com.shield.module.accounting.dto;

import com.shield.module.accounting.entity.LedgerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerCreateRequest(
        @NotNull LedgerType type,
        @NotBlank String category,
        @NotNull @Positive BigDecimal amount,
        String reference,
        String description,
        @NotNull LocalDate entryDate
) {
}
