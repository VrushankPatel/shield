package com.shield.module.parking.dto;

import com.shield.module.parking.entity.ParkingType;
import com.shield.module.parking.entity.VehicleType;
import java.time.Instant;
import java.util.UUID;

public record ParkingSlotResponse(
        UUID id,
        UUID tenantId,
        String slotNumber,
        ParkingType parkingType,
        VehicleType vehicleType,
        UUID unitId,
        boolean allocated,
        Instant allocatedAt
) {
}
