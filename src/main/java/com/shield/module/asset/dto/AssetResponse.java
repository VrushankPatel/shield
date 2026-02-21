package com.shield.module.asset.dto;

import com.shield.module.asset.entity.AssetStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        UUID tenantId,
        String assetCode,
        String assetName,
        UUID categoryId,
        String category,
        String location,
        String blockName,
        String floorLabel,
        AssetStatus status,
        LocalDate purchaseDate,
        LocalDate installationDate,
        LocalDate warrantyExpiryDate,
        boolean amcApplicable,
        UUID amcVendorId,
        LocalDate amcStartDate,
        LocalDate amcEndDate,
        BigDecimal purchaseCost,
        BigDecimal currentValue,
        String qrCodeData
) {
}
