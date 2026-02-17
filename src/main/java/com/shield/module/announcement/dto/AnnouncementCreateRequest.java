package com.shield.module.announcement.dto;

import com.shield.module.announcement.entity.AnnouncementCategory;
import com.shield.module.announcement.entity.AnnouncementPriority;
import com.shield.module.announcement.entity.AnnouncementTargetAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record AnnouncementCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 20000) String content,
        @NotNull AnnouncementCategory category,
        @NotNull AnnouncementPriority priority,
        boolean emergency,
        Instant expiresAt,
        @NotNull AnnouncementTargetAudience targetAudience
) {
}
