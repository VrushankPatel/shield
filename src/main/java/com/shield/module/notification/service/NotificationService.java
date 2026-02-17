package com.shield.module.notification.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
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
import java.util.ArrayList;
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
                .map(email -> new DispatchTarget(null, email))
                .toList();

        NotificationDispatchResponse response = dispatch(
                principal.tenantId(),
                targets,
                request.subject(),
                request.body(),
                "MANUAL",
                principal.userId());

        auditLogService.record(
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

        auditLogService.record(
                tenantId,
                initiatedBy,
                "ANNOUNCEMENT_EMAIL_DISPATCHED",
                "announcement",
                announcementId,
                null);
        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationLogResponse> list(Pageable pageable) {
        return PagedResponse.from(notificationEmailLogRepository.findAllByDeletedFalse(pageable).map(this::toLogResponse));
    }

    @Transactional(readOnly = true)
    public NotificationLogResponse getById(UUID id) {
        NotificationEmailLogEntity entity = notificationEmailLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification log not found: " + id));
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
            if (!isEligibleForEmail(tenantId, target.userId())) {
                persistLog(tenantId, target, subject, body, NotificationDeliveryStatus.SKIPPED,
                        "Recipient disabled email notifications", sourceType, sourceId, null);
                skipped++;
                continue;
            }

            if (!emailEnabled) {
                persistLog(tenantId, target, subject, body, NotificationDeliveryStatus.SKIPPED,
                        "Email notifications are disabled", sourceType, sourceId, null);
                skipped++;
                continue;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(target.email());
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);

                persistLog(tenantId, target, subject, body, NotificationDeliveryStatus.SENT,
                        null, sourceType, sourceId, Instant.now());
                sent++;
            } catch (Exception ex) {
                persistLog(tenantId, target, subject, body, NotificationDeliveryStatus.FAILED,
                        ex.getMessage(), sourceType, sourceId, null);
                failed++;
            }
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
            String subject,
            String body,
            NotificationDeliveryStatus status,
            String error,
            String sourceType,
            UUID sourceId,
            Instant sentAt) {

        NotificationEmailLogEntity log = new NotificationEmailLogEntity();
        log.setTenantId(tenantId);
        log.setUserId(target.userId());
        log.setRecipientEmail(target.email());
        log.setSubject(subject);
        log.setBody(body);
        log.setStatus(status);
        log.setErrorMessage(error);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setSentAt(sentAt);
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
}
