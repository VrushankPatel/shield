package com.shield.module.complaint.dto;

import com.shield.module.complaint.entity.ComplaintPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ComplaintCreateRequest(
        UUID assetId,
        @NotNull UUID unitId,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull ComplaintPriority priority
) {
}
