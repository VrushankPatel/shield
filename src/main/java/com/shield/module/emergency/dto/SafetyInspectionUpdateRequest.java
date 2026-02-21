package com.shield.module.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record SafetyInspectionUpdateRequest(
        @NotNull UUID equipmentId,
        @NotNull LocalDate inspectionDate,
        UUID inspectedBy,
        @NotBlank @Size(max = 50) String inspectionResult,
        @Size(max = 2000) String remarks
) {
}
