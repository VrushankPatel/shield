package com.shield.module.asset.dto;

import com.shield.module.asset.entity.MaintenanceFrequency;
import java.time.LocalDate;
import java.util.UUID;

public record PreventiveMaintenanceResponse(
        UUID id,
        UUID tenantId,
        UUID assetId,
        String maintenanceType,
        MaintenanceFrequency frequency,
        LocalDate lastMaintenanceDate,
        LocalDate nextMaintenanceDate,
        UUID assignedVendorId,
        boolean active
) {
}
