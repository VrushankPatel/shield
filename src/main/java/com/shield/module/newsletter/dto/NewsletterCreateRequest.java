package com.shield.module.newsletter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsletterCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 20000) String content,
        @Size(max = 1000) String summary,
        @Size(max = 1000) String fileUrl,
        @Min(2000) @Max(2100) int year,
        @Min(1) @Max(12) int month) {
}
