package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AccountHeadUpdateRequest(
        @NotBlank String headName,
        @NotBlank String headType,
        UUID parentHeadId) {
}
