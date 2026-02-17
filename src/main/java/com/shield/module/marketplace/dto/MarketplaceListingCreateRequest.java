package com.shield.module.marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MarketplaceListingCreateRequest(
        UUID categoryId,
        @NotBlank @Size(max = 50) String listingType,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 20000) String description,
        @DecimalMin("0.0") BigDecimal price,
        boolean negotiable,
        @Size(max = 2000) String images,
        UUID unitId,
        Instant expiresAt
) {
}
