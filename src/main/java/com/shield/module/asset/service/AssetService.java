package com.shield.module.asset.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.asset.dto.AssetCreateRequest;
import com.shield.module.asset.dto.AssetResponse;
import com.shield.module.asset.dto.AssetUpdateRequest;
import com.shield.module.asset.entity.AssetEntity;
import com.shield.module.asset.repository.AssetRepository;
import com.shield.tenant.context.TenantContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    public AssetResponse create(AssetCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        AssetEntity entity = new AssetEntity();
        entity.setTenantId(tenantId);
        entity.setAssetCode(request.assetCode());
        entity.setCategory(request.category());
        entity.setLocation(request.location());
        entity.setStatus(request.status());
        entity.setPurchaseDate(request.purchaseDate());

        AssetEntity saved = assetRepository.save(entity);
        auditLogService.record(tenantId, null, "ASSET_CREATED", "asset", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> list(Pageable pageable) {
        return PagedResponse.from(assetRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AssetResponse getById(UUID id) {
        AssetEntity entity = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
        return toResponse(entity);
    }

    public AssetResponse update(UUID id, AssetUpdateRequest request) {
        AssetEntity entity = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));

        entity.setAssetCode(request.assetCode());
        entity.setCategory(request.category());
        entity.setLocation(request.location());
        entity.setStatus(request.status());
        entity.setPurchaseDate(request.purchaseDate());

        AssetEntity saved = assetRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "ASSET_UPDATED", "asset", saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        AssetEntity entity = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));

        entity.setDeleted(true);
        assetRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "ASSET_DELETED", "asset", entity.getId(), null);
    }

    private AssetResponse toResponse(AssetEntity entity) {
        return new AssetResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAssetCode(),
                entity.getCategory(),
                entity.getLocation(),
                entity.getStatus(),
                entity.getPurchaseDate());
    }
}
