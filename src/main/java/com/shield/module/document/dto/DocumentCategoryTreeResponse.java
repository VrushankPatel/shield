package com.shield.module.document.dto;

import java.util.List;
import java.util.UUID;

public record DocumentCategoryTreeResponse(
        UUID id,
        UUID tenantId,
        String categoryName,
        String description,
        UUID parentCategoryId,
        List<DocumentCategoryTreeResponse> children
) {
}
