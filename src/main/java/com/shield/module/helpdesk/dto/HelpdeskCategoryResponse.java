package com.shield.module.helpdesk.dto;

import java.util.UUID;

public record HelpdeskCategoryResponse(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        Integer slaHours
) {
}
