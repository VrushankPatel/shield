package com.shield.module.parking.dto;

import com.shield.module.parking.entity.ParkingType;
import com.shield.module.parking.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ParkingSlotUpdateRequest(
        @NotBlank @Size(max = 50) String slotNumber,
        @NotNull ParkingType parkingType,
        @NotNull VehicleType vehicleType,
        UUID unitId
) {
}
