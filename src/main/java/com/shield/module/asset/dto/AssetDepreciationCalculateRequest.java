package com.shield.module.asset.dto;

import com.shield.module.asset.entity.DepreciationMethod;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AssetDepreciationCalculateRequest(
        @NotNull UUID assetId,
        @NotNull DepreciationMethod depreciationMethod,
        @NotNull BigDecimal depreciationRate,
        @NotNull Integer depreciationYear,
        BigDecimal baseValue
) {
}
