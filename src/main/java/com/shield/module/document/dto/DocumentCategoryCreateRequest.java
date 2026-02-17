package com.shield.module.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DocumentCategoryCreateRequest(
        @NotBlank @Size(max = 100) String categoryName,
        @Size(max = 500) String description,
        UUID parentCategoryId
) {
}
