package com.shield.module.config.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TenantConfigBulkUpdateRequest(
        @NotEmpty List<@Valid TenantConfigBulkUpdateItem> entries
) {
}
