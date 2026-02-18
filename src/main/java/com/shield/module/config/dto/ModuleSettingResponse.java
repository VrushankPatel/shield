package com.shield.module.config.dto;

import java.time.Instant;

public record ModuleSettingResponse(
        String module,
        boolean enabled,
        Instant updatedAt
) {
}
