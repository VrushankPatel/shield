package com.shield.module.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.notification.dto.NotificationDispatchResponse;
import com.shield.module.notification.dto.NotificationSendRequest;
import com.shield.module.notification.entity.NotificationDeliveryStatus;
import com.shield.module.notification.entity.NotificationEmailLogEntity;
import com.shield.module.notification.repository.NotificationEmailLogRepository;
import com.shield.module.notification.repository.NotificationPreferenceRepository;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        when(notificationEmailLogRepository.save(any(NotificationEmailLogEntity.class)))
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
}
