package com.shield.module.asset.dto;

import com.shield.module.asset.entity.DepreciationMethod;
import java.math.BigDecimal;
import java.util.UUID;

public record AssetDepreciationResponse(
        UUID id,
        UUID tenantId,
        UUID assetId,
        DepreciationMethod depreciationMethod,
        BigDecimal depreciationRate,
        Integer depreciationYear,
        BigDecimal depreciationAmount,
        BigDecimal bookValue
) {
}
