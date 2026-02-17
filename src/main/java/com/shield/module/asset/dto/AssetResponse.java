package com.shield.module.asset.dto;

import com.shield.module.asset.entity.AssetStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        UUID tenantId,
        String assetCode,
        String category,
        String location,
        AssetStatus status,
        LocalDate purchaseDate
) {
}
