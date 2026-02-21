package com.shield.module.asset.dto;

import com.shield.module.asset.entity.MaintenanceFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record PreventiveMaintenanceCreateRequest(
        @NotNull UUID assetId,
        @NotBlank String maintenanceType,
        @NotNull MaintenanceFrequency frequency,
        @NotNull LocalDate nextMaintenanceDate,
        UUID assignedVendorId,
        Boolean active
) {
}
