package com.shield.module.unit.dto;

import com.shield.module.unit.entity.UnitOwnershipStatus;
import java.time.Instant;
import java.util.UUID;

public record UnitOwnershipUpdateResponse(
        UUID unitId,
        UnitOwnershipStatus previousOwnershipStatus,
        UnitOwnershipStatus ownershipStatus,
        UUID changedBy,
        Instant changedAt,
        String notes
) {
}
