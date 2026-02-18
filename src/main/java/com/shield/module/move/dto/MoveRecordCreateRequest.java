package com.shield.module.move.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MoveRecordCreateRequest(
        @NotNull UUID unitId,
        @NotNull UUID userId,
        @NotNull LocalDate effectiveDate,
        @DecimalMin("0.0") BigDecimal securityDeposit,
        @Size(max = 2000) String agreementUrl
) {
}
