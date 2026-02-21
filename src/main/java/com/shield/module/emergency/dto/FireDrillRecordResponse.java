package com.shield.module.emergency.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record FireDrillRecordResponse(
        UUID id,
        UUID tenantId,
        LocalDate drillDate,
        LocalTime drillTime,
        UUID conductedBy,
        Integer evacuationTime,
        Integer participantsCount,
        String observations,
        String reportUrl
) {
}
