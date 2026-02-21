package com.shield.module.asset.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetCategoryUpdateRequest(
        @NotBlank String categoryName,
        String description
) {
}
