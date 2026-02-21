package com.shield.module.unit.dto;

import com.shield.module.unit.entity.UnitOwnershipStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnitOwnershipUpdateRequest(
        @NotNull UnitOwnershipStatus ownershipStatus,
        @Size(max = 500) String notes
) {
}
