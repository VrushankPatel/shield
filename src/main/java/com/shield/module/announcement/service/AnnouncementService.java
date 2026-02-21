package com.shield.module.announcement.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.announcement.dto.AnnouncementCreateRequest;
import com.shield.module.announcement.dto.AnnouncementPublishResponse;
import com.shield.module.announcement.dto.AnnouncementReadReceiptResponse;
import com.shield.module.announcement.dto.AnnouncementResponse;
import com.shield.module.announcement.dto.AnnouncementStatisticsResponse;
import com.shield.module.announcement.entity.AnnouncementCategory;
import com.shield.module.announcement.entity.AnnouncementEntity;
import com.shield.module.announcement.entity.AnnouncementPriority;
import com.shield.module.announcement.entity.AnnouncementReadReceiptEntity;
import com.shield.module.announcement.entity.AnnouncementStatus;
import com.shield.module.announcement.entity.AnnouncementTargetAudience;
import com.shield.module.announcement.repository.AnnouncementReadReceiptRepository;
import com.shield.module.announcement.repository.AnnouncementRepository;
import com.shield.module.notification.dto.NotificationDispatchResponse;
import com.shield.module.notification.service.NotificationService;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnnouncementService {

    private static final String ENTITY_ANNOUNCEMENT = "announcement";
    private static final String ENTITY_ANNOUNCEMENT_READ_RECEIPT = "announcement_read_receipt";

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadReceiptRepository readReceiptRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            AnnouncementReadReceiptRepository readReceiptRepository,
            NotificationService notificationService,
            AuditLogService auditLogService) {
        this.announcementRepository = announcementRepository;
        this.readReceiptRepository = readReceiptRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    public AnnouncementResponse create(AnnouncementCreateRequest request, ShieldPrincipal principal) {
        AnnouncementEntity entity = new AnnouncementEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setCategory(request.category());
        entity.setPriority(request.priority());
        entity.setEmergency(request.emergency());
        entity.setExpiresAt(request.expiresAt());
        entity.setTargetAudience(request.targetAudience());
        entity.setStatus(AnnouncementStatus.DRAFT);

        AnnouncementEntity saved = announcementRepository.save(entity);
        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                "ANNOUNCEMENT_CREATED",
                ENTITY_ANNOUNCEMENT,
                saved.getId(),
                null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnnouncementResponse> list(Pageable pageable) {
        return PagedResponse.from(announcementRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnnouncementResponse> listByCategory(AnnouncementCategory category, Pageable pageable) {
        return PagedResponse.from(announcementRepository.findAllByCategoryAndDeletedFalse(category, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnnouncementResponse> listByPriority(AnnouncementPriority priority, Pageable pageable) {
        return PagedResponse.from(announcementRepository.findAllByPriorityAndDeletedFalse(priority, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnnouncementResponse> listActive(Pageable pageable) {
        return PagedResponse.from(announcementRepository.findAllActive(Instant.now(), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse getById(UUID id) {
        return toResponse(findAnnouncement(id));
    }

    public AnnouncementPublishResponse publish(UUID id, ShieldPrincipal principal) {
        AnnouncementEntity entity = findAnnouncement(id);

        entity.setPublishedAt(Instant.now());
        entity.setPublishedBy(principal.userId());
        entity.setStatus(AnnouncementStatus.PUBLISHED);
        AnnouncementEntity saved = announcementRepository.save(entity);

        List<UserEntity> activeUsers = notificationService.getTenantActiveUsers(principal.tenantId());
        List<UserEntity> recipients = filterAudience(activeUsers, saved.getTargetAudience());

        String subject = "[Announcement] " + saved.getTitle();
        NotificationDispatchResponse dispatchResponse = notificationService.sendAnnouncement(
                principal.tenantId(),
                principal.userId(),
                saved.getId(),
                subject,
                saved.getContent(),
                recipients);

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                "ANNOUNCEMENT_PUBLISHED",
                ENTITY_ANNOUNCEMENT,
                saved.getId(),
                null);
        return new AnnouncementPublishResponse(toResponse(saved), dispatchResponse);
    }

    public AnnouncementReadReceiptResponse markRead(UUID announcementId, ShieldPrincipal principal) {
        AnnouncementEntity announcement = findAnnouncement(announcementId);
        if (announcement.getStatus() != AnnouncementStatus.PUBLISHED) {
            throw new BadRequestException("Only published announcements can be marked as read");
        }

        AnnouncementReadReceiptEntity existing = readReceiptRepository
                .findByAnnouncementIdAndUserIdAndDeletedFalse(announcementId, principal.userId())
                .orElse(null);
        if (existing != null) {
            return toReadReceiptResponse(existing);
        }

        AnnouncementReadReceiptEntity receipt = new AnnouncementReadReceiptEntity();
        receipt.setTenantId(principal.tenantId());
        receipt.setAnnouncementId(announcementId);
        receipt.setUserId(principal.userId());
        receipt.setReadAt(Instant.now());
        AnnouncementReadReceiptEntity saved = readReceiptRepository.save(receipt);

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                "ANNOUNCEMENT_MARK_READ",
                ENTITY_ANNOUNCEMENT_READ_RECEIPT,
                saved.getId(),
                null);

        return toReadReceiptResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnnouncementReadReceiptResponse> listReadReceipts(UUID announcementId, Pageable pageable) {
        findAnnouncement(announcementId);
        return PagedResponse.from(
                readReceiptRepository.findAllByAnnouncementIdAndDeletedFalse(announcementId, pageable)
                        .map(this::toReadReceiptResponse));
    }

    @Transactional(readOnly = true)
    public AnnouncementStatisticsResponse getStatistics(UUID announcementId) {
        AnnouncementEntity announcement = findAnnouncement(announcementId);

        List<UserEntity> activeUsers = notificationService.getTenantActiveUsers(announcement.getTenantId());
        long totalRecipients = filterAudience(activeUsers, announcement.getTargetAudience()).size();
        long readCount = readReceiptRepository.countByAnnouncementIdAndDeletedFalse(announcementId);
        long unreadCount = Math.max(totalRecipients - readCount, 0);

        return new AnnouncementStatisticsResponse(
                announcementId,
                totalRecipients,
                readCount,
                readCount,
                unreadCount);
    }

    private AnnouncementEntity findAnnouncement(UUID id) {
        return announcementRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
    }

    private List<UserEntity> filterAudience(List<UserEntity> users, AnnouncementTargetAudience audience) {
        return switch (audience) {
            case ALL -> users;
            case OWNERS -> byRole(users, UserRole.OWNER);
            case TENANTS -> byRole(users, UserRole.TENANT);
            case COMMITTEE -> byRole(users, UserRole.COMMITTEE);
            case SECURITY -> byRole(users, UserRole.SECURITY);
        };
    }

    private List<UserEntity> byRole(List<UserEntity> users, UserRole role) {
        return users.stream().filter(user -> user.getRole() == role).toList();
    }

    private AnnouncementResponse toResponse(AnnouncementEntity entity) {
        return new AnnouncementResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getCategory(),
                entity.getPriority(),
                entity.isEmergency(),
                entity.getPublishedBy(),
                entity.getPublishedAt(),
                entity.getExpiresAt(),
                entity.getTargetAudience(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private AnnouncementReadReceiptResponse toReadReceiptResponse(AnnouncementReadReceiptEntity entity) {
        return new AnnouncementReadReceiptResponse(
                entity.getId(),
                entity.getAnnouncementId(),
                entity.getUserId(),
                entity.getReadAt(),
                entity.getCreatedAt());
    }
}
