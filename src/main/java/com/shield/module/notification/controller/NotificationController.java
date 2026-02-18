package com.shield.module.notification.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.notification.dto.NotificationBulkSendRequest;
import com.shield.module.notification.dto.NotificationDispatchResponse;
import com.shield.module.notification.dto.NotificationLogResponse;
import com.shield.module.notification.dto.NotificationSendRequest;
import com.shield.module.notification.service.NotificationService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<NotificationDispatchResponse>> send(
            @Valid @RequestBody NotificationSendRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(
                ApiResponse.ok("Notification dispatch completed", notificationService.sendManual(request, principal)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Notification logs fetched", notificationService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Notification log fetched", notificationService.getById(id)));
    }

    @PostMapping("/send-bulk")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> sendBulk(@Valid @RequestBody NotificationBulkSendRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        notificationService.sendBulk(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Bulk notifications sent", null));
    }

    @PostMapping("/{id}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        notificationService.markRead(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        notificationService.markAllRead(principal);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Unread count fetched", notificationService.getUnreadCount(principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        notificationService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted", null));
    }
}
