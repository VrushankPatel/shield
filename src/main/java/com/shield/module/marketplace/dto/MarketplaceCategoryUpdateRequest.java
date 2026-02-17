package com.shield.module.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarketplaceCategoryUpdateRequest(
        @NotBlank @Size(max = 100) String categoryName,
        @Size(max = 500) String description
) {
}
