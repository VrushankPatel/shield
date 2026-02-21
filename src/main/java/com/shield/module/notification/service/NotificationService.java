package com.shield.module.notification.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.notification.dto.NotificationBulkSendRequest;
import com.shield.module.notification.dto.NotificationDispatchResponse;
import com.shield.module.notification.dto.NotificationLogResponse;
import com.shield.module.notification.dto.NotificationPreferenceResponse;
import com.shield.module.notification.dto.NotificationPreferenceUpdateRequest;
import com.shield.module.notification.dto.NotificationSendRequest;
import com.shield.module.notification.entity.NotificationDeliveryStatus;
import com.shield.module.notification.entity.NotificationEmailLogEntity;
import com.shield.module.notification.entity.NotificationPreferenceEntity;
import com.shield.module.notification.repository.NotificationEmailLogRepository;
import com.shield.module.notification.repository.NotificationPreferenceRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private static final String EMAIL_DISABLED_REASON = "Email notifications are disabled";
    private static final String EMAIL_PREFERENCE_DISABLED_REASON = "Recipient disabled email notifications";

    private final JavaMailSender mailSender;
    private final NotificationEmailLogRepository notificationEmailLogRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final boolean emailEnabled;
    private final String fromAddress;

    public NotificationService(
            JavaMailSender mailSender,
            NotificationEmailLogRepository notificationEmailLogRepository,
            NotificationPreferenceRepository notificationPreferenceRepository,
            UserRepository userRepository,
            AuditLogService auditLogService,
            @Value("${shield.notification.email.enabled:false}") boolean emailEnabled,
            @Value("${shield.notification.email.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.notificationEmailLogRepository = notificationEmailLogRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
    }

    public NotificationDispatchResponse sendManual(NotificationSendRequest request, ShieldPrincipal principal) {
        List<DispatchTarget> targets = request.recipients().stream()
                .distinct()
                .map(email -> resolveDispatchTarget(principal.tenantId(), email))
                .toList();

        NotificationDispatchResponse response = dispatch(
                principal.tenantId(),
                targets,
                request.subject(),
                request.body(),
                "MANUAL",
                principal.userId());

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                "NOTIFICATION_MANUAL_SENT",
                "notification_email_log",
                null,
                null);
        return response;
    }

    public NotificationDispatchResponse sendAnnouncement(
            UUID tenantId,
            UUID initiatedBy,
            UUID announcementId,
            String subject,
            String body,
            List<UserEntity> recipients) {

        List<DispatchTarget> targets = recipients.stream()
                .map(user -> new DispatchTarget(user.getId(), user.getEmail()))
                .toList();

        NotificationDispatchResponse response = dispatch(
                tenantId,
                targets,
                subject,
                body,
                "ANNOUNCEMENT",
                announcementId);

        auditLogService.logEvent(
                tenantId,
                initiatedBy,
                "ANNOUNCEMENT_EMAIL_DISPATCHED",
                "announcement",
                announcementId,
                null);
        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationLogResponse> list(Pageable pageable, ShieldPrincipal principal) {
        if (isPrivileged(principal)) {
            return PagedResponse
                    .from(notificationEmailLogRepository.findAllByDeletedFalse(pageable).map(this::toLogResponse));
        }
        return PagedResponse.from(
                notificationEmailLogRepository.findAllByUserIdAndDeletedFalse(principal.userId(), pageable)
                        .map(this::toLogResponse));
    }

    @Transactional(readOnly = true)
    public NotificationLogResponse getById(UUID id, ShieldPrincipal principal) {
        NotificationEmailLogEntity entity = findAccessibleNotification(id, principal);
        return toLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getTenantActiveUsers(UUID tenantId) {
        return userRepository.findAllByTenantIdAndStatusAndDeletedFalse(tenantId, UserStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreference(UUID tenantId, UUID userId) {
        return notificationPreferenceRepository.findByTenantIdAndUserIdAndDeletedFalse(tenantId, userId)
                .map(this::toPreferenceResponse)
                .orElse(new NotificationPreferenceResponse(null, tenantId, userId, true, null, null));
    }

    public NotificationPreferenceResponse updatePreference(
            UUID tenantId,
            UUID userId,
            NotificationPreferenceUpdateRequest request) {

        NotificationPreferenceEntity entity = notificationPreferenceRepository
                .findByTenantIdAndUserIdAndDeletedFalse(tenantId, userId)
                .orElseGet(() -> {
                    NotificationPreferenceEntity preference = new NotificationPreferenceEntity();
                    preference.setTenantId(tenantId);
                    preference.setUserId(userId);
                    return preference;
                });

        entity.setEmailEnabled(request.emailEnabled());
        NotificationPreferenceEntity saved = notificationPreferenceRepository.save(entity);
        return toPreferenceResponse(saved);
    }

    private NotificationDispatchResponse dispatch(
            UUID tenantId,
            List<DispatchTarget> targets,
            String subject,
            String body,
            String sourceType,
            UUID sourceId) {

        int sent = 0;
        int failed = 0;
        int skipped = 0;

        for (DispatchTarget target : targets) {
            DispatchStatus dispatchStatus;
            if (!isEligibleForEmail(tenantId, target.userId())) {
                dispatchStatus = new DispatchStatus(NotificationDeliveryStatus.SKIPPED, EMAIL_PREFERENCE_DISABLED_REASON, null);
                skipped++;
            } else if (!emailEnabled) {
                dispatchStatus = new DispatchStatus(NotificationDeliveryStatus.SKIPPED, EMAIL_DISABLED_REASON, null);
                skipped++;
            } else {
                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromAddress);
                    message.setTo(target.email());
                    message.setSubject(subject);
                    message.setText(body);
                    mailSender.send(message);

                    dispatchStatus = new DispatchStatus(NotificationDeliveryStatus.SENT, null, Instant.now());
                    sent++;
                } catch (Exception ex) {
                    dispatchStatus = new DispatchStatus(NotificationDeliveryStatus.FAILED, ex.getMessage(), null);
                    failed++;
                }
            }

            persistLog(
                    tenantId,
                    target,
                    new DispatchLog(
                            subject,
                            body,
                            dispatchStatus.status(),
                            dispatchStatus.error(),
                            sourceType,
                            sourceId,
                            dispatchStatus.sentAt()));
        }

        return new NotificationDispatchResponse(targets.size(), sent, failed, skipped);
    }

    private boolean isEligibleForEmail(UUID tenantId, UUID userId) {
        if (userId == null) {
            return true;
        }

        return notificationPreferenceRepository.findByTenantIdAndUserIdAndDeletedFalse(tenantId, userId)
                .map(NotificationPreferenceEntity::isEmailEnabled)
                .orElse(true);
    }

    private void persistLog(
            UUID tenantId,
            DispatchTarget target,
            DispatchLog dispatchLog) {

        NotificationEmailLogEntity log = new NotificationEmailLogEntity();
        log.setTenantId(tenantId);
        log.setUserId(target.userId());
        log.setRecipientEmail(target.email());
        log.setSubject(dispatchLog.subject());
        log.setBody(dispatchLog.body());
        log.setStatus(dispatchLog.status());
        log.setErrorMessage(dispatchLog.error());
        log.setSourceType(dispatchLog.sourceType());
        log.setSourceId(dispatchLog.sourceId());
        log.setSentAt(dispatchLog.sentAt());
        notificationEmailLogRepository.save(log);
    }

    private NotificationLogResponse toLogResponse(NotificationEmailLogEntity entity) {
        return new NotificationLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getRecipientEmail(),
                entity.getSubject(),
                entity.getBody(),
                entity.getStatus(),
                entity.getErrorMessage(),
                entity.getSourceType(),
                entity.getSourceId(),
                entity.getSentAt(),
                entity.getCreatedAt());
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreferenceEntity entity) {
        return new NotificationPreferenceResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.isEmailEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private record DispatchTarget(UUID userId, String email) {
    }

    private record DispatchLog(
            String subject,
            String body,
            NotificationDeliveryStatus status,
            String error,
            String sourceType,
            UUID sourceId,
            Instant sentAt) {
    }

    private record DispatchStatus(NotificationDeliveryStatus status, String error, Instant sentAt) {
    }

    private DispatchTarget resolveDispatchTarget(UUID tenantId, String recipientEmail) {
        return userRepository.findByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, recipientEmail)
                .map(user -> new DispatchTarget(user.getId(), user.getEmail()))
                .orElse(new DispatchTarget(null, recipientEmail));
    }

    private boolean isPrivileged(ShieldPrincipal principal) {
        return "ADMIN".equals(principal.role()) || "COMMITTEE".equals(principal.role());
    }

    private NotificationEmailLogEntity findAccessibleNotification(UUID notificationId, ShieldPrincipal principal) {
        if (isPrivileged(principal)) {
            return notificationEmailLogRepository.findByIdAndDeletedFalse(notificationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        }
        return notificationEmailLogRepository.findByIdAndUserIdAndDeletedFalse(notificationId, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
    }

    public void sendBulk(NotificationBulkSendRequest request, ShieldPrincipal principal) {
        request.notifications().forEach(req -> sendManual(req, principal));
    }

    public void markRead(UUID notificationId, ShieldPrincipal principal) {
        NotificationEmailLogEntity entity = findAccessibleNotification(notificationId, principal);

        if (entity.getReadAt() == null) {
            entity.setReadAt(Instant.now());
            notificationEmailLogRepository.save(entity);
        }
    }

    public void markAllRead(ShieldPrincipal principal) {
        List<NotificationEmailLogEntity> unread = notificationEmailLogRepository
                .findAllByUserIdAndDeletedFalse(principal.userId())
                .stream()
                .filter(n -> n.getReadAt() == null)
                .toList();

        Instant now = Instant.now();
        unread.forEach(n -> n.setReadAt(now));
        notificationEmailLogRepository.saveAll(unread);
    }

    public long getUnreadCount(ShieldPrincipal principal) {
        if (isPrivileged(principal)) {
            return 0L;
        }
        return notificationEmailLogRepository.countByUserIdAndReadAtIsNullAndDeletedFalse(principal.userId());
    }

    public void delete(UUID notificationId, ShieldPrincipal principal) {
        NotificationEmailLogEntity entity = findAccessibleNotification(notificationId, principal);

        entity.setDeleted(true);
        notificationEmailLogRepository.save(entity);
    }
}
