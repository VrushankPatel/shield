package com.shield.module.asset.dto;

import com.shield.module.asset.entity.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssetUpdateRequest(
        @NotBlank String assetCode,
        String assetName,
        UUID categoryId,
        @NotBlank String category,
        String location,
        String blockName,
        String floorLabel,
        @NotNull AssetStatus status,
        LocalDate purchaseDate,
        LocalDate installationDate,
        LocalDate warrantyExpiryDate,
        Boolean amcApplicable,
        UUID amcVendorId,
        LocalDate amcStartDate,
        LocalDate amcEndDate,
        BigDecimal purchaseCost,
        BigDecimal currentValue,
        String qrCodeData
) {
}
