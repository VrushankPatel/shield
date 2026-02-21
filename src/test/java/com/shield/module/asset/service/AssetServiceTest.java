package com.shield.module.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.asset.dto.AssetCreateRequest;
import com.shield.module.asset.dto.AssetResponse;
import com.shield.module.asset.dto.AssetUpdateRequest;
import com.shield.module.asset.entity.AssetEntity;
import com.shield.module.asset.entity.AssetStatus;
import com.shield.module.asset.repository.AssetRepository;
import com.shield.tenant.context.TenantContext;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AuditLogService auditLogService;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetRepository, auditLogService);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createShouldPersistAsset() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(assetRepository.save(any(AssetEntity.class))).thenAnswer(invocation -> {
            AssetEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        AssetResponse response = assetService.create(new AssetCreateRequest(
                "A1B",
                "Lobby bulb",
                null,
                "ELECTRICAL",
                "Block A",
                "A",
                "GROUND",
                AssetStatus.ACTIVE,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 1, 1),
                true,
                UUID.randomUUID(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                null,
                null,
                "ASSET-QR-1"));

        assertEquals(tenantId, response.tenantId());
        assertEquals("A1B", response.assetCode());
        assertEquals(AssetStatus.ACTIVE, response.status());
        assertEquals("ASSET-QR-1", response.qrCodeData());
    }

    @Test
    void getByTagShouldThrowWhenAssetMissing() {
        when(assetRepository.findByAssetCodeAndDeletedFalse("A1B")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> assetService.getByTag("A1B"));
    }

    @Test
    void updateShouldThrowWhenAssetMissing() {
        UUID id = UUID.randomUUID();
        when(assetRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        LocalDate today = LocalDate.now();
        AssetUpdateRequest request = new AssetUpdateRequest(
                "A1B",
                "Lobby bulb",
                null,
                "ELECTRICAL",
                "Block A",
                "A",
                "GROUND",
                AssetStatus.ACTIVE,
                today,
                today,
                today.plusMonths(6),
                true,
                UUID.randomUUID(),
                today,
                today.plusMonths(6),
                null,
                null,
                "ASSET-QR-1");

        assertThrows(ResourceNotFoundException.class, () -> assetService.update(id, request));
    }

    @Test
    void deleteShouldSoftDeleteAsset() {
        UUID id = UUID.randomUUID();

        AssetEntity entity = new AssetEntity();
        entity.setId(id);
        entity.setTenantId(UUID.randomUUID());

        when(assetRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(assetRepository.save(any(AssetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assetService.delete(id);

        assertEquals(true, entity.isDeleted());
    }
}
