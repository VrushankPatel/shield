package com.shield.module.config.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record JsonSettingResponse(
        String key,
        String group,
        JsonNode value,
        Instant updatedAt
) {
}
