package com.shield.module.announcement.dto;

import com.shield.module.announcement.entity.AnnouncementCategory;
import com.shield.module.announcement.entity.AnnouncementPriority;
import com.shield.module.announcement.entity.AnnouncementStatus;
import com.shield.module.announcement.entity.AnnouncementTargetAudience;
import java.time.Instant;
import java.util.UUID;

public record AnnouncementResponse(
        UUID id,
        UUID tenantId,
        String title,
        String content,
        AnnouncementCategory category,
        AnnouncementPriority priority,
        boolean emergency,
        UUID publishedBy,
        Instant publishedAt,
        Instant expiresAt,
        AnnouncementTargetAudience targetAudience,
        AnnouncementStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
