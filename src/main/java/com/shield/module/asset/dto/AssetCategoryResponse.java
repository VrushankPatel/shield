package com.shield.module.asset.dto;

import java.util.UUID;

public record AssetCategoryResponse(
        UUID id,
        UUID tenantId,
        String categoryName,
        String description
) {
}
