package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AccountHeadCreateRequest(
        @NotBlank String headName,
        @NotBlank String headType,
        UUID parentHeadId) {
}
