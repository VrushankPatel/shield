package com.shield.module.parking.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ParkingSlotAllocateRequest(
        @NotNull UUID unitId
) {
}
