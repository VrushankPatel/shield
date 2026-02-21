package com.shield.module.emergency.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SafetyInspectionResponse(
        UUID id,
        UUID tenantId,
        UUID equipmentId,
        LocalDate inspectionDate,
        UUID inspectedBy,
        String inspectionResult,
        String remarks
) {
}
