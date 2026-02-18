package com.shield.module.newsletter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsletterUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 20000) String content,
        @Size(max = 1000) String summary,
        @Size(max = 1000) String fileUrl) {
}
