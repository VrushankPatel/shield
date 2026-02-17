package com.shield.module.utility.dto;

import java.util.UUID;

public record ElectricityMeterResponse(
        UUID id,
        UUID tenantId,
        String meterNumber,
        String meterType,
        String location,
        UUID unitId
) {
}
