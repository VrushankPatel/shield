package com.shield.module.file.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneratePresignedUrlRequest(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 150) String contentType,
        @Min(1) @Max(1440) Integer expiresInMinutes
) {
}
