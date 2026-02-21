package com.shield.module.emergency.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SafetyEquipmentResponse(
        UUID id,
        UUID tenantId,
        String equipmentType,
        String equipmentTag,
        String location,
        LocalDate installationDate,
        LocalDate lastInspectionDate,
        LocalDate nextInspectionDate,
        Integer inspectionFrequencyDays,
        boolean functional
) {
}
