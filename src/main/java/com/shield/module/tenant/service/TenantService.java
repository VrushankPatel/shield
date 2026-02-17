package com.shield.module.tenant.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.tenant.dto.TenantCreateRequest;
import com.shield.module.tenant.dto.TenantResponse;
import com.shield.module.tenant.dto.TenantUpdateRequest;
import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.mapper.TenantMapper;
import com.shield.module.tenant.repository.TenantRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final AuditLogService auditLogService;

    public TenantResponse create(TenantCreateRequest request) {
        TenantEntity tenant = new TenantEntity();
        tenant.setName(request.name());
        tenant.setAddress(request.address());

        TenantEntity saved = tenantRepository.save(tenant);
        auditLogService.record(saved.getId(), null, "TENANT_CREATED", "tenant", saved.getId(), null);
        return tenantMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TenantResponse getById(UUID id) {
        TenantEntity tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));
        return tenantMapper.toResponse(tenant);
    }

    public TenantResponse update(UUID id, TenantUpdateRequest request) {
        TenantEntity tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));

        tenant.setName(request.name());
        tenant.setAddress(request.address());
        TenantEntity saved = tenantRepository.save(tenant);
        auditLogService.record(saved.getId(), null, "TENANT_UPDATED", "tenant", saved.getId(), null);
        return tenantMapper.toResponse(saved);
    }
}
