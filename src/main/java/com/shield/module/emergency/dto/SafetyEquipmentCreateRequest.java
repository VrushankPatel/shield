package com.shield.module.emergency.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SafetyEquipmentCreateRequest(
        @NotBlank @Size(max = 100) String equipmentType,
        @Size(max = 100) String equipmentTag,
        @Size(max = 255) String location,
        LocalDate installationDate,
        LocalDate lastInspectionDate,
        LocalDate nextInspectionDate,
        @Min(0) Integer inspectionFrequencyDays,
        Boolean functional
) {
}
