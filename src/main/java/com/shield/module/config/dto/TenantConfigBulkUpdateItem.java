package com.shield.module.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantConfigBulkUpdateItem(
        @NotBlank @Size(max = 100) String key,
        @Size(max = 20000) String value,
        @Size(max = 50) String category
) {
}
