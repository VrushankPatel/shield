package com.shield.module.visitor.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BlacklistResponse(
        UUID id,
        UUID tenantId,
        String personName,
        String phone,
        String reason,
        UUID blacklistedBy,
        LocalDate blacklistDate,
        boolean active
) {
}
