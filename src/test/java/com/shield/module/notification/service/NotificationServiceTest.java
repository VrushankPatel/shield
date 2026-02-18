package com.shield.module.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.dto.PagedResponse;
import com.shield.module.notification.dto.NotificationDispatchResponse;
import com.shield.module.notification.dto.NotificationLogResponse;
import com.shield.module.notification.dto.NotificationSendRequest;
import com.shield.module.notification.entity.NotificationDeliveryStatus;
import com.shield.module.notification.entity.NotificationEmailLogEntity;
import com.shield.module.notification.repository.NotificationEmailLogRepository;
import com.shield.module.notification.repository.NotificationPreferenceRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationEmailLogRepository notificationEmailLogRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private NotificationService disabledNotificationService;
    private NotificationService enabledNotificationService;

    @BeforeEach
    void setUp() {
        lenient().when(notificationEmailLogRepository.save(any(NotificationEmailLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        disabledNotificationService = new NotificationService(
                mailSender,
                notificationEmailLogRepository,
                notificationPreferenceRepository,
                userRepository,
                auditLogService,
                false,
                "no-reply@shield.local");

        enabledNotificationService = new NotificationService(
                mailSender,
                notificationEmailLogRepository,
                notificationPreferenceRepository,
                userRepository,
                auditLogService,
                true,
                "no-reply@shield.local");
    }

    @Test
    void sendManualShouldSkipWhenEmailIsDisabled() {
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        NotificationSendRequest request = new NotificationSendRequest(
                List.of("owner1@shield.dev", "owner2@shield.dev"),
                "Subject",
                "Body");

        NotificationDispatchResponse response = disabledNotificationService.sendManual(request, principal);

        assertEquals(2, response.total());
        assertEquals(0, response.sent());
        assertEquals(0, response.failed());
        assertEquals(2, response.skipped());

        ArgumentCaptor<NotificationEmailLogEntity> captor = ArgumentCaptor.forClass(NotificationEmailLogEntity.class);
        verify(notificationEmailLogRepository, times(2)).save(captor.capture());
        captor.getAllValues().forEach(log -> assertEquals(NotificationDeliveryStatus.SKIPPED, log.getStatus()));
        verify(mailSender, never()).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void sendManualShouldSendWhenEmailIsEnabled() {
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        NotificationSendRequest request = new NotificationSendRequest(
                List.of("owner1@shield.dev"),
                "Subject",
                "Body");

        NotificationDispatchResponse response = enabledNotificationService.sendManual(request, principal);

        assertEquals(1, response.total());
        assertEquals(1, response.sent());
        assertEquals(0, response.failed());
        assertEquals(0, response.skipped());

        verify(mailSender, times(1)).send(any(org.springframework.mail.SimpleMailMessage.class));
        verify(auditLogService).record(eq(principal.tenantId()), eq(principal.userId()), eq("NOTIFICATION_MANUAL_SENT"), eq("notification_email_log"), eq(null), eq(null));
    }

    @Test
    void sendManualShouldLinkRecipientToTenantUserWhenFound() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String recipient = "owner1@shield.dev";
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");
        NotificationSendRequest request = new NotificationSendRequest(List.of(recipient), "Subject", "Body");

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail(recipient);
        when(userRepository.findByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, recipient))
                .thenReturn(Optional.of(user));

        disabledNotificationService.sendManual(request, principal);

        ArgumentCaptor<NotificationEmailLogEntity> captor = ArgumentCaptor.forClass(NotificationEmailLogEntity.class);
        verify(notificationEmailLogRepository).save(captor.capture());
        NotificationEmailLogEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(recipient, saved.getRecipientEmail());
    }

    @Test
    void listShouldUseGlobalQueryForPrivilegedUsers() {
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        PageRequest pageable = PageRequest.of(0, 20);
        Page<NotificationEmailLogEntity> page = new PageImpl<>(List.of(buildLog(UUID.randomUUID())));
        when(notificationEmailLogRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        PagedResponse<NotificationLogResponse> response = disabledNotificationService.list(pageable, principal);

        assertEquals(1, response.content().size());
        verify(notificationEmailLogRepository).findAllByDeletedFalse(pageable);
        verify(notificationEmailLogRepository, never()).findAllByUserIdAndDeletedFalse(any(UUID.class), any(PageRequest.class));
    }

    @Test
    void listShouldUseUserScopedQueryForNonPrivilegedUsers() {
        UUID userId = UUID.randomUUID();
        ShieldPrincipal principal = new ShieldPrincipal(userId, UUID.randomUUID(), "resident@shield.dev", "TENANT");
        PageRequest pageable = PageRequest.of(0, 20);
        Page<NotificationEmailLogEntity> page = new PageImpl<>(List.of(buildLog(userId)));
        when(notificationEmailLogRepository.findAllByUserIdAndDeletedFalse(userId, pageable)).thenReturn(page);

        PagedResponse<NotificationLogResponse> response = disabledNotificationService.list(pageable, principal);

        assertEquals(1, response.content().size());
        verify(notificationEmailLogRepository).findAllByUserIdAndDeletedFalse(userId, pageable);
        verify(notificationEmailLogRepository, never()).findAllByDeletedFalse(any(PageRequest.class));
    }

    @Test
    void getByIdShouldRejectNonOwnerForNonPrivilegedUsers() {
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "resident@shield.dev", "TENANT");
        UUID notificationId = UUID.randomUUID();
        when(notificationEmailLogRepository.findByIdAndUserIdAndDeletedFalse(notificationId, principal.userId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> disabledNotificationService.getById(notificationId, principal));
    }

    @Test
    void markReadShouldUpdateReadAtWhenUnread() {
        UUID userId = UUID.randomUUID();
        ShieldPrincipal principal = new ShieldPrincipal(userId, UUID.randomUUID(), "resident@shield.dev", "TENANT");
        UUID notificationId = UUID.randomUUID();
        NotificationEmailLogEntity entity = buildLog(userId);
        entity.setId(notificationId);
        entity.setReadAt(null);
        when(notificationEmailLogRepository.findByIdAndUserIdAndDeletedFalse(notificationId, userId))
                .thenReturn(Optional.of(entity));

        disabledNotificationService.markRead(notificationId, principal);

        verify(notificationEmailLogRepository).save(entity);
    }

    private NotificationEmailLogEntity buildLog(UUID userId) {
        NotificationEmailLogEntity entity = new NotificationEmailLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setRecipientEmail("resident@shield.dev");
        entity.setSubject("Subject");
        entity.setBody("Body");
        entity.setStatus(NotificationDeliveryStatus.SENT);
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
