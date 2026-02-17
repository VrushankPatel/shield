package com.shield.module.announcement.dto;

import com.shield.module.notification.dto.NotificationDispatchResponse;

public record AnnouncementPublishResponse(
        AnnouncementResponse announcement,
        NotificationDispatchResponse notificationDispatch
) {
}
