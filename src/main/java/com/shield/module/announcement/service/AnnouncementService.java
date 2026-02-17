package com.shield.module.announcement.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.announcement.dto.AnnouncementCreateRequest;
import com.shield.module.announcement.dto.AnnouncementPublishResponse;
import com.shield.module.announcement.dto.AnnouncementResponse;
import com.shield.module.announcement.entity.AnnouncementEntity;
import com.shield.module.announcement.entity.AnnouncementStatus;
import com.shield.module.announcement.entity.AnnouncementTargetAudience;
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

    private final AnnouncementRepository announcementRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            NotificationService notificationService,
            AuditLogService auditLogService) {
        this.announcementRepository = announcementRepository;
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
        auditLogService.record(principal.tenantId(), principal.userId(), "ANNOUNCEMENT_CREATED", "announcement", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnnouncementResponse> list(Pageable pageable) {
        return PagedResponse.from(announcementRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse getById(UUID id) {
        AnnouncementEntity entity = announcementRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
        return toResponse(entity);
    }

    public AnnouncementPublishResponse publish(UUID id, ShieldPrincipal principal) {
        AnnouncementEntity entity = announcementRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));

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

        auditLogService.record(principal.tenantId(), principal.userId(), "ANNOUNCEMENT_PUBLISHED", "announcement", saved.getId(), null);
        return new AnnouncementPublishResponse(toResponse(saved), dispatchResponse);
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
}
