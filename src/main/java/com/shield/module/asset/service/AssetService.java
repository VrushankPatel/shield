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
import java.time.LocalDate;
import java.util.List;
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
        applyValues(entity, request);

        AssetEntity saved = assetRepository.save(entity);
        auditLogService.record(tenantId, null, "ASSET_CREATED", "asset", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> list(Pageable pageable) {
        return PagedResponse.from(assetRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> listByCategory(UUID categoryId, Pageable pageable) {
        return PagedResponse.from(assetRepository.findAllByCategoryIdAndDeletedFalse(categoryId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> listByLocation(String location, Pageable pageable) {
        return PagedResponse.from(assetRepository.findAllByLocationContainingIgnoreCaseAndDeletedFalse(location, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AssetResponse getByTag(String tag) {
        AssetEntity entity = assetRepository.findByAssetCodeAndDeletedFalse(tag)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with tag: " + tag));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public AssetResponse verifyQr(String qrCode) {
        AssetEntity entity = assetRepository.findByQrCodeDataAndDeletedFalse(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for QR code: " + qrCode));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> listAmcExpiring(int days, Pageable pageable) {
        LocalDate now = LocalDate.now();
        LocalDate until = now.plusDays(Math.max(days, 0));
        return PagedResponse.from(assetRepository.findAllByAmcApplicableTrueAndAmcEndDateBetweenAndDeletedFalse(now, until, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> listWarrantyExpiring(int days, Pageable pageable) {
        LocalDate now = LocalDate.now();
        LocalDate until = now.plusDays(Math.max(days, 0));
        return PagedResponse.from(assetRepository.findAllByWarrantyExpiryDateBetweenAndDeletedFalse(now, until, pageable)
                .map(this::toResponse));
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

        applyValues(entity, request);

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

    @Transactional(readOnly = true)
    public String exportCsv() {
        List<AssetEntity> assets = assetRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("id,assetCode,assetName,categoryId,category,status,location,blockName,floorLabel,warrantyExpiryDate,amcEndDate,qrCodeData\n");
        for (AssetEntity asset : assets) {
            csv.append(csv(asset.getId())).append(',')
                    .append(csv(asset.getAssetCode())).append(',')
                    .append(csv(asset.getAssetName())).append(',')
                    .append(csv(asset.getCategoryId())).append(',')
                    .append(csv(asset.getCategory())).append(',')
                    .append(csv(asset.getStatus())).append(',')
                    .append(csv(asset.getLocation())).append(',')
                    .append(csv(asset.getBlockName())).append(',')
                    .append(csv(asset.getFloorLabel())).append(',')
                    .append(csv(asset.getWarrantyExpiryDate())).append(',')
                    .append(csv(asset.getAmcEndDate())).append(',')
                    .append(csv(asset.getQrCodeData()))
                    .append('\n');
        }
        return csv.toString();
    }

    private void applyValues(AssetEntity entity, AssetCreateRequest request) {
        entity.setAssetCode(request.assetCode());
        entity.setAssetName(request.assetName());
        entity.setCategoryId(request.categoryId());
        entity.setCategory(request.category());
        entity.setLocation(request.location());
        entity.setBlockName(request.blockName());
        entity.setFloorLabel(request.floorLabel());
        entity.setStatus(request.status());
        entity.setPurchaseDate(request.purchaseDate());
        entity.setInstallationDate(request.installationDate());
        entity.setWarrantyExpiryDate(request.warrantyExpiryDate());
        entity.setAmcApplicable(Boolean.TRUE.equals(request.amcApplicable()));
        entity.setAmcVendorId(request.amcVendorId());
        entity.setAmcStartDate(request.amcStartDate());
        entity.setAmcEndDate(request.amcEndDate());
        entity.setPurchaseCost(request.purchaseCost());
        entity.setCurrentValue(request.currentValue());
        entity.setQrCodeData(request.qrCodeData());
    }

    private void applyValues(AssetEntity entity, AssetUpdateRequest request) {
        entity.setAssetCode(request.assetCode());
        entity.setAssetName(request.assetName());
        entity.setCategoryId(request.categoryId());
        entity.setCategory(request.category());
        entity.setLocation(request.location());
        entity.setBlockName(request.blockName());
        entity.setFloorLabel(request.floorLabel());
        entity.setStatus(request.status());
        entity.setPurchaseDate(request.purchaseDate());
        entity.setInstallationDate(request.installationDate());
        entity.setWarrantyExpiryDate(request.warrantyExpiryDate());
        entity.setAmcApplicable(Boolean.TRUE.equals(request.amcApplicable()));
        entity.setAmcVendorId(request.amcVendorId());
        entity.setAmcStartDate(request.amcStartDate());
        entity.setAmcEndDate(request.amcEndDate());
        entity.setPurchaseCost(request.purchaseCost());
        entity.setCurrentValue(request.currentValue());
        entity.setQrCodeData(request.qrCodeData());
    }

    private AssetResponse toResponse(AssetEntity entity) {
        return new AssetResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAssetCode(),
                entity.getAssetName(),
                entity.getCategoryId(),
                entity.getCategory(),
                entity.getLocation(),
                entity.getBlockName(),
                entity.getFloorLabel(),
                entity.getStatus(),
                entity.getPurchaseDate(),
                entity.getInstallationDate(),
                entity.getWarrantyExpiryDate(),
                entity.isAmcApplicable(),
                entity.getAmcVendorId(),
                entity.getAmcStartDate(),
                entity.getAmcEndDate(),
                entity.getPurchaseCost(),
                entity.getCurrentValue(),
                entity.getQrCodeData());
    }

    private String csv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = value.toString().replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
