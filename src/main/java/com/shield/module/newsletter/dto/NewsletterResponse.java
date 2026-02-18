package com.shield.module.newsletter.dto;

import com.shield.module.newsletter.entity.NewsletterStatus;
import java.time.Instant;
import java.util.UUID;

public record NewsletterResponse(
        UUID id,
        UUID tenantId,
        String title,
        String content,
        String summary,
        String fileUrl,
        int year,
        int month,
        NewsletterStatus status,
        Instant publishedAt,
        UUID publishedBy,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt) {
}
