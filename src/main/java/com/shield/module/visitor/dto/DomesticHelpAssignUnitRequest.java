package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record DomesticHelpAssignUnitRequest(
        @NotNull UUID unitId,
        LocalDate startDate,
        LocalDate endDate
) {
}
