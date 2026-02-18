package com.shield.module.config.dto;

import jakarta.validation.constraints.Size;

public record TenantConfigUpsertRequest(
        @Size(max = 20000) String value,
        @Size(max = 50) String category
) {
}
