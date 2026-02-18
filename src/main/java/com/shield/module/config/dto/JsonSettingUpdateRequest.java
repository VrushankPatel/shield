package com.shield.module.config.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record JsonSettingUpdateRequest(
        @NotNull JsonNode value
) {
}
