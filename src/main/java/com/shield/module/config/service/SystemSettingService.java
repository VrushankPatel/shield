package com.shield.module.config.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.config.dto.JsonSettingResponse;
import com.shield.module.config.dto.JsonSettingUpdateRequest;
import com.shield.module.config.dto.ModuleSettingResponse;
import com.shield.module.config.dto.ModuleToggleRequest;
import com.shield.module.config.entity.SystemSettingEntity;
import com.shield.module.config.repository.SystemSettingRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemSettingService {

    private static final String MODULE_GROUP = "MODULES";
    private static final String MODULE_KEY_PREFIX = "module.";
    private static final String MODULE_KEY_SUFFIX = ".enabled";

    private static final String BILLING_GROUP = "BILLING";
    private static final String BILLING_KEY = "billing.formula";

    private static final String SLA_GROUP = "SLA";
    private static final String SLA_KEY = "sla.rules";

    private static final String NOTIFICATION_GROUP = "NOTIFICATION";
    private static final String NOTIFICATION_KEY = "notification.channels";

    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public SystemSettingService(
            SystemSettingRepository systemSettingRepository,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.systemSettingRepository = systemSettingRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ModuleSettingResponse> listModules() {
        return systemSettingRepository.findAllBySettingGroupAndDeletedFalse(MODULE_GROUP).stream()
                .filter(setting -> setting.getSettingKey() != null
                        && setting.getSettingKey().startsWith(MODULE_KEY_PREFIX)
                        && setting.getSettingKey().endsWith(MODULE_KEY_SUFFIX))
                .map(this::toModuleResponse)
                .sorted(Comparator.comparing(ModuleSettingResponse::module))
                .toList();
    }

    public ModuleSettingResponse toggleModule(String module, ModuleToggleRequest request, ShieldPrincipal principal) {
        String normalizedModule = module.trim().toLowerCase(Locale.ROOT);
        String key = MODULE_KEY_PREFIX + normalizedModule + MODULE_KEY_SUFFIX;

        SystemSettingEntity entity = upsertStringSetting(
                key,
                MODULE_GROUP,
                Boolean.toString(request.enabled()),
                principal,
                "MODULE_SETTING_TOGGLED");

        return toModuleResponse(entity);
    }

    @Transactional(readOnly = true)
    public JsonSettingResponse getBillingFormula() {
        return getJsonSetting(BILLING_KEY, BILLING_GROUP);
    }

    public JsonSettingResponse updateBillingFormula(JsonSettingUpdateRequest request, ShieldPrincipal principal) {
        return upsertJsonSetting(BILLING_KEY, BILLING_GROUP, request.value(), principal, "BILLING_FORMULA_UPDATED");
    }

    @Transactional(readOnly = true)
    public JsonSettingResponse getSlaRules() {
        return getJsonSetting(SLA_KEY, SLA_GROUP);
    }

    public JsonSettingResponse updateSlaRules(JsonSettingUpdateRequest request, ShieldPrincipal principal) {
        return upsertJsonSetting(SLA_KEY, SLA_GROUP, request.value(), principal, "SLA_RULES_UPDATED");
    }

    @Transactional(readOnly = true)
    public JsonSettingResponse getNotificationChannels() {
        return getJsonSetting(NOTIFICATION_KEY, NOTIFICATION_GROUP);
    }

    public JsonSettingResponse updateNotificationChannels(JsonSettingUpdateRequest request, ShieldPrincipal principal) {
        return upsertJsonSetting(
                NOTIFICATION_KEY,
                NOTIFICATION_GROUP,
                request.value(),
                principal,
                "NOTIFICATION_CHANNELS_UPDATED");
    }

    private JsonSettingResponse getJsonSetting(String key, String group) {
        return systemSettingRepository
                .findFirstBySettingKeyAndDeletedFalseOrderByCreatedAtDesc(key)
                .map(this::toJsonResponse)
                .orElse(new JsonSettingResponse(key, group, objectMapper.createObjectNode(), null));
    }

    private JsonSettingResponse upsertJsonSetting(
            String key,
            String group,
            JsonNode value,
            ShieldPrincipal principal,
            String auditAction) {

        SystemSettingEntity entity = upsertStringSetting(
                key,
                group,
                toJsonText(value),
                principal,
                auditAction);

        return toJsonResponse(entity);
    }

    private SystemSettingEntity upsertStringSetting(
            String key,
            String group,
            String value,
            ShieldPrincipal principal,
            String auditAction) {

        SystemSettingEntity entity = systemSettingRepository
                .findFirstBySettingKeyAndDeletedFalseOrderByCreatedAtDesc(key)
                .orElseGet(() -> {
                    SystemSettingEntity setting = new SystemSettingEntity();
                    setting.setTenantId(principal.tenantId());
                    setting.setSettingKey(key);
                    setting.setSettingGroup(group);
                    return setting;
                });

        entity.setSettingValue(value);
        entity.setSettingGroup(group);

        SystemSettingEntity saved = systemSettingRepository.save(entity);

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                auditAction,
                "system_setting",
                saved.getId(),
                null);

        return saved;
    }

    private ModuleSettingResponse toModuleResponse(SystemSettingEntity entity) {
        String module = entity.getSettingKey();
        if (module.startsWith(MODULE_KEY_PREFIX)) {
            module = module.substring(MODULE_KEY_PREFIX.length());
        }
        if (module.endsWith(MODULE_KEY_SUFFIX)) {
            module = module.substring(0, module.length() - MODULE_KEY_SUFFIX.length());
        }

        return new ModuleSettingResponse(
                module,
                Boolean.parseBoolean(entity.getSettingValue()),
                entity.getUpdatedAt());
    }

    private JsonSettingResponse toJsonResponse(SystemSettingEntity entity) {
        return new JsonSettingResponse(
                entity.getSettingKey(),
                entity.getSettingGroup(),
                parseJson(entity.getSettingValue()),
                entity.getUpdatedAt());
    }

    private String toJsonText(JsonNode value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Invalid JSON setting payload");
        }
    }

    private JsonNode parseJson(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(rawValue);
        } catch (JsonProcessingException ex) {
            return objectMapper.getNodeFactory().textNode(rawValue);
        }
    }
}
