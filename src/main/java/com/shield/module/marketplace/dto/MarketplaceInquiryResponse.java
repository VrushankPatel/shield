package com.shield.module.marketplace.dto;

import java.time.Instant;
import java.util.UUID;

public record MarketplaceInquiryResponse(
        UUID id,
        UUID tenantId,
        UUID listingId,
        UUID inquiredBy,
        String message,
        Instant createdAt
) {
}
