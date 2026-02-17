package com.shield.module.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HelpdeskCategoryUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        Integer slaHours
) {
}
