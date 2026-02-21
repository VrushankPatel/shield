package com.shield.module.announcement.dto;

import java.util.UUID;

public record AnnouncementStatisticsResponse(
        UUID announcementId,
        long totalRecipients,
        long totalReads,
        long uniqueReaders,
        long unreadCount
) {
}
