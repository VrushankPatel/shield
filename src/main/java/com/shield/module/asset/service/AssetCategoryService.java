package com.shield.module.asset.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
import com.shield.module.asset.dto.AssetCategoryCreateRequest;
import com.shield.module.asset.dto.AssetCategoryResponse;
import com.shield.module.asset.dto.AssetCategoryUpdateRequest;
import com.shield.module.asset.entity.AssetCategoryEntity;
import com.shield.module.asset.repository.AssetCategoryRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetCategoryService {

    private static final String ENTITY_ASSET_CATEGORY = "asset_category";

    private final AssetCategoryRepository assetCategoryRepository;
    private final AuditLogService auditLogService;

    public AssetCategoryResponse create(AssetCategoryCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();

        AssetCategoryEntity entity = new AssetCategoryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setCategoryName(request.categoryName().trim());
        entity.setDescription(trimToNull(request.description()));

        AssetCategoryEntity saved = assetCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "ASSET_CATEGORY_CREATED", ENTITY_ASSET_CATEGORY, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetCategoryResponse> list(Pageable pageable) {
        return PagedResponse.from(assetCategoryRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AssetCategoryResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public AssetCategoryResponse update(UUID id, AssetCategoryUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        AssetCategoryEntity entity = findById(id);
        entity.setCategoryName(request.categoryName().trim());
        entity.setDescription(trimToNull(request.description()));

        AssetCategoryEntity saved = assetCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "ASSET_CATEGORY_UPDATED", ENTITY_ASSET_CATEGORY, saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        AssetCategoryEntity entity = findById(id);
        entity.setDeleted(true);
        assetCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "ASSET_CATEGORY_DELETED", ENTITY_ASSET_CATEGORY, id, null);
    }

    private AssetCategoryEntity findById(UUID id) {
        return assetCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset category not found: " + id));
    }

    private AssetCategoryResponse toResponse(AssetCategoryEntity entity) {
        return new AssetCategoryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getCategoryName(),
                entity.getDescription());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
