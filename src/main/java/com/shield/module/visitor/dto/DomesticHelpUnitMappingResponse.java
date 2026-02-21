package com.shield.module.visitor.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DomesticHelpUnitMappingResponse(
        UUID id,
        UUID tenantId,
        UUID domesticHelpId,
        UUID unitId,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
}
