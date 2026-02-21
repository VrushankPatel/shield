package com.shield.module.asset.dto;

import java.math.BigDecimal;

public record AssetDepreciationReportRow(
        Integer depreciationYear,
        long records,
        BigDecimal totalDepreciation,
        BigDecimal totalBookValue
) {
}
