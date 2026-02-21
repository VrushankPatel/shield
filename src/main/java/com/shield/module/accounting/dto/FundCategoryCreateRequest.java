package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record FundCategoryCreateRequest(
        @NotBlank String categoryName,
        String description,
        @NotNull @PositiveOrZero BigDecimal currentBalance) {
}
