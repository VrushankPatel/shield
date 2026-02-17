package com.shield.module.document.dto;

import java.util.UUID;

public record DocumentCategoryResponse(
        UUID id,
        UUID tenantId,
        String categoryName,
        String description,
        UUID parentCategoryId
) {
}
