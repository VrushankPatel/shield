package com.shield.module.announcement.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.announcement.dto.AnnouncementAttachmentRequest;
import com.shield.module.announcement.dto.AnnouncementAttachmentResponse;
import com.shield.module.announcement.dto.AnnouncementCreateRequest;
import com.shield.module.announcement.dto.AnnouncementPublishResponse;
import com.shield.module.announcement.dto.AnnouncementReadReceiptResponse;
import com.shield.module.announcement.dto.AnnouncementResponse;
import com.shield.module.announcement.dto.AnnouncementStatisticsResponse;
import com.shield.module.announcement.entity.AnnouncementCategory;
import com.shield.module.announcement.entity.AnnouncementPriority;
import com.shield.module.announcement.service.AnnouncementAttachmentService;
import com.shield.module.announcement.service.AnnouncementService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final AnnouncementAttachmentService attachmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @Valid @RequestBody AnnouncementCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity
                .ok(ApiResponse.ok("Announcement created", announcementService.create(request, principal)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AnnouncementResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Announcements fetched", announcementService.list(pageable)));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PagedResponse<AnnouncementResponse>>> listByCategory(
            @PathVariable AnnouncementCategory category,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Announcements fetched",
                announcementService.listByCategory(category, pageable)));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<PagedResponse<AnnouncementResponse>>> listByPriority(
            @PathVariable AnnouncementPriority priority,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Announcements fetched",
                announcementService.listByPriority(priority, pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<AnnouncementResponse>>> listActive(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Announcements fetched", announcementService.listActive(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Announcement fetched", announcementService.getById(id)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnnouncementPublishResponse>> publish(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Announcement published", announcementService.publish(id, principal)));
    }

    @PostMapping("/{id}/mark-read")
    public ResponseEntity<ApiResponse<AnnouncementReadReceiptResponse>> markRead(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Announcement marked as read", announcementService.markRead(id, principal)));
    }

    @GetMapping("/{id}/read-receipts")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<AnnouncementReadReceiptResponse>>> listReadReceipts(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Announcement read receipts fetched",
                announcementService.listReadReceipts(id, pageable)));
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnnouncementStatisticsResponse>> statistics(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Announcement statistics fetched", announcementService.getStatistics(id)));
    }

    @PostMapping("/{id}/attachments")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnnouncementAttachmentResponse>> addAttachment(
            @PathVariable UUID id,
            @Valid @RequestBody AnnouncementAttachmentRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity
                .ok(ApiResponse.ok("Attachment added", attachmentService.addAttachment(id, request, principal)));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<List<AnnouncementAttachmentResponse>>> listAttachments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Attachments fetched", attachmentService.listAttachments(id)));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable UUID attachmentId) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        attachmentService.deleteAttachment(attachmentId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Attachment deleted", null));
    }
}
