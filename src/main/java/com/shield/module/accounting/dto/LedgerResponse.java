package com.shield.module.accounting.dto;

import com.shield.module.accounting.entity.LedgerType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LedgerResponse(
        UUID id,
        UUID tenantId,
        LedgerType type,
        String category,
        BigDecimal amount,
        String reference,
        String description,
        LocalDate entryDate
) {
}
