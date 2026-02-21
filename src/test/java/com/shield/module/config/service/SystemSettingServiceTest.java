package com.shield.module.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shield.audit.service.AuditLogService;
import com.shield.module.config.dto.JsonSettingUpdateRequest;
import com.shield.module.config.dto.ModuleToggleRequest;
import com.shield.module.config.entity.SystemSettingEntity;
import com.shield.module.config.repository.SystemSettingRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private SystemSettingService systemSettingService;

    @BeforeEach
    void setUp() {
        systemSettingService = new SystemSettingService(systemSettingRepository, auditLogService, objectMapper);
    }

    @Test
    void listModulesShouldFilterAndSortEntries() {
        SystemSettingEntity one = moduleSetting("module.billing.enabled", "true");
        SystemSettingEntity two = moduleSetting("module.analytics.enabled", "false");
        SystemSettingEntity ignored = moduleSetting("billing.formula", "{}");

        when(systemSettingRepository.findAllBySettingGroupAndDeletedFalse("MODULES"))
                .thenReturn(List.of(one, ignored, two));

        var modules = systemSettingService.listModules();

        assertEquals(2, modules.size());
        assertEquals("analytics", modules.get(0).module());
        assertFalse(modules.get(0).enabled());
        assertEquals("billing", modules.get(1).module());
        assertTrue(modules.get(1).enabled());
    }

    @Test
    void toggleModuleShouldUpsertSettingAndAudit() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(systemSettingRepository.findFirstBySettingKeyAndDeletedFalseOrderByCreatedAtDesc("module.billing.enabled"))
                .thenReturn(Optional.empty());
        when(systemSettingRepository.save(any(SystemSettingEntity.class))).thenAnswer(invocation -> {
            SystemSettingEntity entity = invocation.getArgument(0);
            entity.setId(id);
            return entity;
        });

        var response = systemSettingService.toggleModule(" Billing ", new ModuleToggleRequest(true), principal(tenantId, userId));

        assertEquals("billing", response.module());
        assertTrue(response.enabled());
        verify(auditLogService).logEvent(tenantId, userId, "MODULE_SETTING_TOGGLED", "system_setting", id, null);
    }

    @Test
    void getBillingFormulaShouldReturnEmptyJsonWhenMissing() {
        when(systemSettingRepository.findFirstBySettingKeyAndDeletedFalseOrderByCreatedAtDesc("billing.formula"))
                .thenReturn(Optional.empty());

        var response = systemSettingService.getBillingFormula();

        assertEquals("billing.formula", response.key());
        assertTrue(response.value().isObject());
        assertEquals(0, response.value().size());
    }

    @Test
    void updateBillingFormulaShouldPersistJsonValue() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ObjectNode formula = objectMapper.createObjectNode();
        formula.put("mode", "HYBRID");
        formula.put("fixedShare", 500);

        when(systemSettingRepository.findFirstBySettingKeyAndDeletedFalseOrderByCreatedAtDesc("billing.formula"))
                .thenReturn(Optional.empty());
        when(systemSettingRepository.save(any(SystemSettingEntity.class))).thenAnswer(invocation -> {
            SystemSettingEntity entity = invocation.getArgument(0);
            entity.setId(id);
            return entity;
        });

        var response = systemSettingService.updateBillingFormula(
                new JsonSettingUpdateRequest(formula),
                principal(tenantId, userId));

        assertEquals("BILLING", response.group());
        assertEquals("HYBRID", response.value().get("mode").asText());
        verify(auditLogService).logEvent(tenantId, userId, "BILLING_FORMULA_UPDATED", "system_setting", id, null);
    }

    @Test
    void getNotificationChannelsShouldParseLegacyStringAsJsonTextNode() {
        SystemSettingEntity entity = new SystemSettingEntity();
        entity.setSettingKey("notification.channels");
        entity.setSettingGroup("NOTIFICATION");
        entity.setSettingValue("EMAIL,SMS");

        when(systemSettingRepository.findFirstBySettingKeyAndDeletedFalseOrderByCreatedAtDesc("notification.channels"))
                .thenReturn(Optional.of(entity));

        var response = systemSettingService.getNotificationChannels();

        assertTrue(response.value().isTextual());
        assertEquals("EMAIL,SMS", response.value().asText());
    }

    private SystemSettingEntity moduleSetting(String key, String value) {
        SystemSettingEntity entity = new SystemSettingEntity();
        entity.setSettingGroup("MODULES");
        entity.setSettingKey(key);
        entity.setSettingValue(value);
        return entity;
    }

    private ShieldPrincipal principal(UUID tenantId, UUID userId) {
        return new ShieldPrincipal(userId, tenantId, "settings@shield.dev", "ADMIN");
    }
}
