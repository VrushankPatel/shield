package com.shield.module.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.config.dto.TenantConfigBulkUpdateItem;
import com.shield.module.config.dto.TenantConfigBulkUpdateRequest;
import com.shield.module.config.dto.TenantConfigUpsertRequest;
import com.shield.module.config.entity.TenantConfigEntity;
import com.shield.module.config.repository.TenantConfigRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TenantConfigServiceTest {

    @Mock
    private TenantConfigRepository tenantConfigRepository;

    @Mock
    private AuditLogService auditLogService;

    private TenantConfigService tenantConfigService;

    @BeforeEach
    void setUp() {
        tenantConfigService = new TenantConfigService(tenantConfigRepository, auditLogService);
    }

    @Test
    void upsertShouldCreateWhenConfigMissing() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        when(tenantConfigRepository.findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc("notifications.email"))
                .thenReturn(Optional.empty());
        when(tenantConfigRepository.save(any(TenantConfigEntity.class))).thenAnswer(invocation -> {
            TenantConfigEntity entity = invocation.getArgument(0);
            entity.setId(configId);
            return entity;
        });

        var response = tenantConfigService.upsert(
                " Notifications.Email ",
                new TenantConfigUpsertRequest("true", " preferences "),
                principal(tenantId, userId));

        assertEquals(configId, response.id());
        assertEquals("notifications.email", response.key());
        assertEquals("preferences", response.category());
        verify(auditLogService).record(tenantId, userId, "TENANT_CONFIG_CREATED", "tenant_config", configId, null);
    }

    @Test
    void upsertShouldUpdateWhenExistingConfigFound() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        TenantConfigEntity existing = new TenantConfigEntity();
        existing.setId(configId);
        existing.setTenantId(tenantId);
        existing.setConfigKey("billing.mode");
        existing.setConfigValue("legacy");

        when(tenantConfigRepository.findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc("billing.mode"))
                .thenReturn(Optional.of(existing));
        when(tenantConfigRepository.save(any(TenantConfigEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = tenantConfigService.upsert(
                "billing.mode",
                new TenantConfigUpsertRequest("modern", "billing"),
                principal(tenantId, userId));

        assertEquals("modern", response.value());
        verify(auditLogService).record(tenantId, userId, "TENANT_CONFIG_UPDATED", "tenant_config", configId, null);
    }

    @Test
    void bulkUpdateShouldUpsertAllEntries() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(tenantConfigRepository.findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc(any()))
                .thenReturn(Optional.empty());
        when(tenantConfigRepository.save(any(TenantConfigEntity.class))).thenAnswer(invocation -> {
            TenantConfigEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = tenantConfigService.bulkUpdate(
                new TenantConfigBulkUpdateRequest(List.of(
                        new TenantConfigBulkUpdateItem("amenity.limit", "5", "amenities"),
                        new TenantConfigBulkUpdateItem("meeting.window", "30", "meetings"))),
                principal(tenantId, userId));

        assertEquals(2, response.size());
    }

    @Test
    void listByCategoryShouldNormalizeBlankToNull() {
        TenantConfigEntity entity = new TenantConfigEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setConfigKey("key");
        entity.setConfigValue("value");

        when(tenantConfigRepository.findAllByCategoryAndDeletedFalse(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), Pageable.ofSize(5), 1));

        var page = tenantConfigService.listByCategory("  ", Pageable.ofSize(5));

        assertEquals(1, page.content().size());
        assertNull(page.content().get(0).category());
    }

    @Test
    void getByKeyShouldThrowWhenMissing() {
        when(tenantConfigRepository.findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc("missing.key"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenantConfigService.getByKey("missing.key"));
    }

    @Test
    void deleteByKeyShouldSoftDeleteAndAudit() {
        UUID configId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TenantConfigEntity entity = new TenantConfigEntity();
        entity.setId(configId);
        entity.setTenantId(tenantId);
        entity.setConfigKey("notification.sms");

        when(tenantConfigRepository.findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc("notification.sms"))
                .thenReturn(Optional.of(entity));
        when(tenantConfigRepository.save(any(TenantConfigEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tenantConfigService.deleteByKey("notification.sms", principal(tenantId, userId));

        assertTrue(entity.isDeleted());
        verify(auditLogService).record(tenantId, userId, "TENANT_CONFIG_DELETED", "tenant_config", configId, null);
    }

    private ShieldPrincipal principal(UUID tenantId, UUID userId) {
        return new ShieldPrincipal(userId, tenantId, "config@shield.dev", "ADMIN");
    }
}
