package com.shield.module.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantUpdateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String address
) {
}
