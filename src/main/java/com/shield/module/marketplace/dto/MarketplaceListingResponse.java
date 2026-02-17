package com.shield.module.marketplace.dto;

import com.shield.module.marketplace.entity.MarketplaceListingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MarketplaceListingResponse(
        UUID id,
        UUID tenantId,
        String listingNumber,
        UUID categoryId,
        String listingType,
        String title,
        String description,
        BigDecimal price,
        boolean negotiable,
        String images,
        UUID postedBy,
        UUID unitId,
        MarketplaceListingStatus status,
        int viewsCount,
        Instant expiresAt
) {
}
