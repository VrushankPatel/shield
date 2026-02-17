package com.shield.module.announcement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.announcement.dto.AnnouncementPublishResponse;
import com.shield.module.announcement.entity.AnnouncementCategory;
import com.shield.module.announcement.entity.AnnouncementEntity;
import com.shield.module.announcement.entity.AnnouncementPriority;
import com.shield.module.announcement.entity.AnnouncementStatus;
import com.shield.module.announcement.entity.AnnouncementTargetAudience;
import com.shield.module.announcement.repository.AnnouncementRepository;
import com.shield.module.notification.dto.NotificationDispatchResponse;
import com.shield.module.notification.service.NotificationService;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.security.model.ShieldPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(announcementRepository, notificationService, auditLogService);
    }

    @Test
    void publishShouldDispatchOnlyTargetAudienceUsers() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID announcementId = UUID.randomUUID();

        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setId(announcementId);
        announcement.setTenantId(tenantId);
        announcement.setTitle("Water Shutdown");
        announcement.setContent("Water supply maintenance window.");
        announcement.setCategory(AnnouncementCategory.MAINTENANCE);
        announcement.setPriority(AnnouncementPriority.HIGH);
        announcement.setTargetAudience(AnnouncementTargetAudience.TENANTS);
        announcement.setStatus(AnnouncementStatus.DRAFT);

        UserEntity tenantUser = new UserEntity();
        tenantUser.setId(UUID.randomUUID());
        tenantUser.setEmail("tenant@shield.dev");
        tenantUser.setRole(UserRole.TENANT);
        tenantUser.setStatus(UserStatus.ACTIVE);

        UserEntity ownerUser = new UserEntity();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setEmail("owner@shield.dev");
        ownerUser.setRole(UserRole.OWNER);
        ownerUser.setStatus(UserStatus.ACTIVE);

        when(announcementRepository.findByIdAndDeletedFalse(announcementId)).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any(AnnouncementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.getTenantActiveUsers(tenantId)).thenReturn(List.of(tenantUser, ownerUser));
        when(notificationService.sendAnnouncement(eq(tenantId), eq(userId), eq(announcementId), any(), any(), any()))
                .thenReturn(new NotificationDispatchResponse(1, 1, 0, 0));

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN");
        AnnouncementPublishResponse response = announcementService.publish(announcementId, principal);

        assertEquals(AnnouncementStatus.PUBLISHED, response.announcement().status());
        assertEquals(1, response.notificationDispatch().total());

        ArgumentCaptor<List<UserEntity>> recipientsCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).sendAnnouncement(eq(tenantId), eq(userId), eq(announcementId), any(), any(), recipientsCaptor.capture());
        assertEquals(1, recipientsCaptor.getValue().size());
        assertEquals(UserRole.TENANT, recipientsCaptor.getValue().get(0).getRole());
    }
}
