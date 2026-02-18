package com.shield.module.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.asset.dto.AssetDepreciationCalculateRequest;
import com.shield.module.asset.dto.AssetDepreciationResponse;
import com.shield.module.asset.entity.AssetDepreciationEntity;
import com.shield.module.asset.entity.AssetEntity;
import com.shield.module.asset.entity.DepreciationMethod;
import com.shield.module.asset.repository.AssetDepreciationRepository;
import com.shield.module.asset.repository.AssetRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AssetDepreciationServiceTest {

    @Mock
    private AssetDepreciationRepository assetDepreciationRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AuditLogService auditLogService;

    private AssetDepreciationService assetDepreciationService;

    @BeforeEach
    void setUp() {
        assetDepreciationService = new AssetDepreciationService(assetDepreciationRepository, assetRepository, auditLogService);
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void calculateShouldUseProvidedBaseValue() {
        UUID assetId = UUID.randomUUID();
        AssetEntity asset = new AssetEntity();
        asset.setId(assetId);

        when(assetRepository.findByIdAndDeletedFalse(assetId)).thenReturn(Optional.of(asset));
        when(assetDepreciationRepository.save(any(AssetDepreciationEntity.class))).thenAnswer(invocation -> {
            AssetDepreciationEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        AssetDepreciationResponse response = assetDepreciationService.calculate(new AssetDepreciationCalculateRequest(
                assetId,
                DepreciationMethod.STRAIGHT_LINE,
                new BigDecimal("10.00"),
                2026,
                new BigDecimal("5000.00")));

        assertEquals(new BigDecimal("500.00"), response.depreciationAmount());
        assertEquals(new BigDecimal("4500.00"), response.bookValue());
    }

    @Test
    void calculateShouldFallbackToPurchaseCostWhenBaseMissing() {
        UUID assetId = UUID.randomUUID();
        AssetEntity asset = new AssetEntity();
        asset.setId(assetId);
        asset.setPurchaseCost(new BigDecimal("12000.00"));

        when(assetRepository.findByIdAndDeletedFalse(assetId)).thenReturn(Optional.of(asset));
        when(assetDepreciationRepository.findAllByAssetIdAndDeletedFalseOrderByDepreciationYearDesc(assetId)).thenReturn(List.of());
        when(assetDepreciationRepository.save(any(AssetDepreciationEntity.class))).thenAnswer(invocation -> {
            AssetDepreciationEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        AssetDepreciationResponse response = assetDepreciationService.calculate(new AssetDepreciationCalculateRequest(
                assetId,
                DepreciationMethod.REDUCING_BALANCE,
                new BigDecimal("5.00"),
                2026,
                null));

        assertEquals(new BigDecimal("600.00"), response.depreciationAmount());
        assertEquals(new BigDecimal("11400.00"), response.bookValue());
    }
}
