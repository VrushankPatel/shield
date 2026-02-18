package com.shield.module.asset.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetCategoryCreateRequest(
        @NotBlank String categoryName,
        String description
) {
}
