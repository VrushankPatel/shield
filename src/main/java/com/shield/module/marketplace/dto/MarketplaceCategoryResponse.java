package com.shield.module.marketplace.dto;

import java.util.UUID;

public record MarketplaceCategoryResponse(
        UUID id,
        UUID tenantId,
        String categoryName,
        String description
) {
}
