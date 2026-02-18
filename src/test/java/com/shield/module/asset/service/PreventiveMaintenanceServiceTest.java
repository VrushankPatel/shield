package com.shield.module.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.asset.dto.PreventiveMaintenanceCreateRequest;
import com.shield.module.asset.dto.PreventiveMaintenanceResponse;
import com.shield.module.asset.entity.AssetEntity;
import com.shield.module.asset.entity.MaintenanceFrequency;
import com.shield.module.asset.entity.PreventiveMaintenanceScheduleEntity;
import com.shield.module.asset.repository.AssetRepository;
import com.shield.module.asset.repository.PreventiveMaintenanceScheduleRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
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
class PreventiveMaintenanceServiceTest {

    @Mock
    private PreventiveMaintenanceScheduleRepository preventiveMaintenanceScheduleRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AuditLogService auditLogService;

    private PreventiveMaintenanceService preventiveMaintenanceService;

    @BeforeEach
    void setUp() {
        preventiveMaintenanceService = new PreventiveMaintenanceService(
                preventiveMaintenanceScheduleRepository,
                assetRepository,
                auditLogService);
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldPersistSchedule() {
        UUID assetId = UUID.randomUUID();
        AssetEntity asset = new AssetEntity();
        asset.setId(assetId);
        when(assetRepository.findByIdAndDeletedFalse(assetId)).thenReturn(Optional.of(asset));
        when(preventiveMaintenanceScheduleRepository.save(any(PreventiveMaintenanceScheduleEntity.class))).thenAnswer(invocation -> {
            PreventiveMaintenanceScheduleEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        PreventiveMaintenanceResponse response = preventiveMaintenanceService.create(new PreventiveMaintenanceCreateRequest(
                assetId,
                "Pump health check",
                MaintenanceFrequency.MONTHLY,
                LocalDate.now().plusDays(3),
                null,
                true));

        assertEquals(assetId, response.assetId());
        assertTrue(response.active());
        assertEquals(MaintenanceFrequency.MONTHLY, response.frequency());
    }

    @Test
    void executeShouldMoveNextDateByFrequency() {
        UUID scheduleId = UUID.randomUUID();
        PreventiveMaintenanceScheduleEntity entity = new PreventiveMaintenanceScheduleEntity();
        entity.setId(scheduleId);
        entity.setTenantId(UUID.randomUUID());
        entity.setFrequency(MaintenanceFrequency.MONTHLY);
        entity.setNextMaintenanceDate(LocalDate.now());

        when(preventiveMaintenanceScheduleRepository.findByIdAndDeletedFalse(scheduleId)).thenReturn(Optional.of(entity));
        when(preventiveMaintenanceScheduleRepository.save(any(PreventiveMaintenanceScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PreventiveMaintenanceResponse response = preventiveMaintenanceService.execute(scheduleId);

        assertEquals(LocalDate.now(), response.lastMaintenanceDate());
        assertEquals(LocalDate.now().plusMonths(1), response.nextMaintenanceDate());
    }
}
