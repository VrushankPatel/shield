package com.shield.module.accounting.dto;

import com.shield.module.accounting.entity.LedgerTransactionType;
import com.shield.module.accounting.entity.LedgerType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id,
        UUID tenantId,
        LedgerType type,
        String category,
        UUID accountHeadId,
        UUID fundCategoryId,
        LedgerTransactionType transactionType,
        BigDecimal amount,
        String reference,
        String referenceType,
        UUID referenceId,
        String description,
        LocalDate entryDate,
        UUID createdBy) {
}
