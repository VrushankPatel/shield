package com.shield.module.accounting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FundBalanceResponse(
        UUID fundCategoryId,
        String categoryName,
        BigDecimal currentBalance) {
}
