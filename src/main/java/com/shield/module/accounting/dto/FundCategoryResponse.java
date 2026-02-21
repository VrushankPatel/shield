package com.shield.module.accounting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FundCategoryResponse(
        UUID id,
        UUID tenantId,
        String categoryName,
        String description,
        BigDecimal currentBalance) {
}
