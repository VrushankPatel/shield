package com.shield.module.config.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.config.dto.TenantConfigBulkUpdateItem;
import com.shield.module.config.dto.TenantConfigBulkUpdateRequest;
import com.shield.module.config.dto.TenantConfigResponse;
import com.shield.module.config.dto.TenantConfigUpsertRequest;
import com.shield.module.config.entity.TenantConfigEntity;
import com.shield.module.config.repository.TenantConfigRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantConfigService {

    private final TenantConfigRepository tenantConfigRepository;
    private final AuditLogService auditLogService;

    public TenantConfigService(
            TenantConfigRepository tenantConfigRepository,
            AuditLogService auditLogService) {
        this.tenantConfigRepository = tenantConfigRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<TenantConfigResponse> list(Pageable pageable) {
        return PagedResponse.from(tenantConfigRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TenantConfigResponse> listByCategory(String category, Pageable pageable) {
        return PagedResponse.from(tenantConfigRepository
                .findAllByCategoryAndDeletedFalse(normalizeNullable(category), pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public TenantConfigResponse getByKey(String key) {
        TenantConfigEntity entity = findByKey(key);
        return toResponse(entity);
    }

    public TenantConfigResponse upsert(String key, TenantConfigUpsertRequest request, ShieldPrincipal principal) {
        return upsertInternal(normalizeKey(key), request.value(), request.category(), principal);
    }

    public List<TenantConfigResponse> bulkUpdate(TenantConfigBulkUpdateRequest request, ShieldPrincipal principal) {
        List<TenantConfigResponse> responses = new ArrayList<>();
        for (TenantConfigBulkUpdateItem entry : request.entries()) {
            responses.add(upsertInternal(entry.key(), entry.value(), entry.category(), principal));
        }
        return responses;
    }

    public void deleteByKey(String key, ShieldPrincipal principal) {
        TenantConfigEntity entity = findByKey(key);
        entity.setDeleted(true);
        tenantConfigRepository.save(entity);

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                "TENANT_CONFIG_DELETED",
                "tenant_config",
                entity.getId(),
                null);
    }

    private TenantConfigResponse upsertInternal(String key, String value, String category, ShieldPrincipal principal) {
        String normalizedKey = normalizeKey(key);
        var existing = tenantConfigRepository.findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc(normalizedKey);
        boolean created = existing.isEmpty();

        TenantConfigEntity entity = existing.orElseGet(() -> {
            TenantConfigEntity tenantConfig = new TenantConfigEntity();
            tenantConfig.setTenantId(principal.tenantId());
            tenantConfig.setConfigKey(normalizedKey);
            return tenantConfig;
        });

        entity.setConfigValue(value);
        entity.setCategory(normalizeNullable(category));

        TenantConfigEntity saved = tenantConfigRepository.save(entity);

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                created ? "TENANT_CONFIG_CREATED" : "TENANT_CONFIG_UPDATED",
                "tenant_config",
                saved.getId(),
                null);

        return toResponse(saved);
    }

    private TenantConfigEntity findByKey(String key) {
        return tenantConfigRepository
                .findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc(normalizeKey(key))
                .orElseThrow(() -> new ResourceNotFoundException("Tenant config not found for key: " + key));
    }

    private String normalizeKey(String key) {
        return key.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private TenantConfigResponse toResponse(TenantConfigEntity entity) {
        return new TenantConfigResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getCategory(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
