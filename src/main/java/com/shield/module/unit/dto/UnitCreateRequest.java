package com.shield.module.unit.dto;

import com.shield.module.unit.entity.UnitStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UnitCreateRequest(
        @NotBlank @Size(max = 50) String unitNumber,
        @Size(max = 50) String block,
        @Size(max = 50) String type,
        @PositiveOrZero BigDecimal squareFeet,
        @NotNull UnitStatus status
) {
}
