package com.shield.module.asset.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
import com.shield.module.asset.dto.AssetDepreciationCalculateRequest;
import com.shield.module.asset.dto.AssetDepreciationReportRow;
import com.shield.module.asset.dto.AssetDepreciationResponse;
import com.shield.module.asset.entity.AssetDepreciationEntity;
import com.shield.module.asset.entity.AssetEntity;
import com.shield.module.asset.repository.AssetDepreciationRepository;
import com.shield.module.asset.repository.AssetRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetDepreciationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final AssetDepreciationRepository assetDepreciationRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    public AssetDepreciationResponse calculate(AssetDepreciationCalculateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        AssetEntity asset = assetRepository.findByIdAndDeletedFalse(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.assetId()));

        BigDecimal baseValue = resolveBaseValue(request, asset);
        BigDecimal depreciationAmount = baseValue
                .multiply(request.depreciationRate())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal bookValue = baseValue.subtract(depreciationAmount);
        if (bookValue.signum() < 0) {
            bookValue = BigDecimal.ZERO;
        }

        AssetDepreciationEntity entity = new AssetDepreciationEntity();
        entity.setTenantId(principal.tenantId());
        entity.setAssetId(request.assetId());
        entity.setDepreciationMethod(request.depreciationMethod());
        entity.setDepreciationRate(request.depreciationRate().setScale(2, RoundingMode.HALF_UP));
        entity.setDepreciationYear(request.depreciationYear());
        entity.setDepreciationAmount(depreciationAmount);
        entity.setBookValue(bookValue.setScale(2, RoundingMode.HALF_UP));

        AssetDepreciationEntity saved = assetDepreciationRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ASSET_DEPRECIATION_CALCULATED", "asset_depreciation", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssetDepreciationResponse> listByAsset(UUID assetId) {
        return assetDepreciationRepository.findAllByAssetIdAndDeletedFalseOrderByDepreciationYearDesc(assetId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetDepreciationResponse> listByYear(Integer year) {
        return assetDepreciationRepository.findAllByDepreciationYearAndDeletedFalseOrderByAssetIdAsc(year)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetDepreciationReportRow> report() {
        List<AssetDepreciationEntity> rows = assetDepreciationRepository.findAllByDeletedFalseOrderByDepreciationYearDesc();
        Map<Integer, List<AssetDepreciationEntity>> byYear = new LinkedHashMap<>();
        for (AssetDepreciationEntity row : rows) {
            byYear.computeIfAbsent(row.getDepreciationYear(), ignored -> new ArrayList<>()).add(row);
        }

        List<AssetDepreciationReportRow> reportRows = new ArrayList<>();
        for (Map.Entry<Integer, List<AssetDepreciationEntity>> entry : byYear.entrySet()) {
            BigDecimal totalDepreciation = BigDecimal.ZERO;
            BigDecimal totalBookValue = BigDecimal.ZERO;
            for (AssetDepreciationEntity entity : entry.getValue()) {
                totalDepreciation = totalDepreciation.add(entity.getDepreciationAmount());
                totalBookValue = totalBookValue.add(entity.getBookValue());
            }
            reportRows.add(new AssetDepreciationReportRow(
                    entry.getKey(),
                    entry.getValue().size(),
                    totalDepreciation.setScale(2, RoundingMode.HALF_UP),
                    totalBookValue.setScale(2, RoundingMode.HALF_UP)));
        }

        reportRows.sort(Comparator.comparing(AssetDepreciationReportRow::depreciationYear).reversed());
        return reportRows;
    }

    private BigDecimal resolveBaseValue(AssetDepreciationCalculateRequest request, AssetEntity asset) {
        if (request.baseValue() != null) {
            return request.baseValue();
        }

        List<AssetDepreciationEntity> previousRows = assetDepreciationRepository.findAllByAssetIdAndDeletedFalseOrderByDepreciationYearDesc(request.assetId());
        if (!previousRows.isEmpty()) {
            return previousRows.get(0).getBookValue();
        }

        if (asset.getCurrentValue() != null) {
            return asset.getCurrentValue();
        }
        if (asset.getPurchaseCost() != null) {
            return asset.getPurchaseCost();
        }
        return BigDecimal.ZERO;
    }

    private AssetDepreciationResponse toResponse(AssetDepreciationEntity entity) {
        return new AssetDepreciationResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAssetId(),
                entity.getDepreciationMethod(),
                entity.getDepreciationRate(),
                entity.getDepreciationYear(),
                entity.getDepreciationAmount(),
                entity.getBookValue());
    }
}
