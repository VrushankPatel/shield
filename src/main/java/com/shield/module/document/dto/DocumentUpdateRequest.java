package com.shield.module.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentUpdateRequest(
        @NotBlank @Size(max = 255) String documentName,
        UUID categoryId,
        @NotBlank @Size(max = 50) String documentType,
        @NotBlank @Size(max = 2000) String fileUrl,
        Long fileSize,
        @Size(max = 1000) String description,
        @Size(max = 50) String versionLabel,
        boolean publicAccess,
        LocalDate expiryDate,
        @Size(max = 500) String tags
) {
}
