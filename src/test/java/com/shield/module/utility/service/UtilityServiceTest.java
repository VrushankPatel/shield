package com.shield.module.utility.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.utility.dto.ElectricityReadingCreateRequest;
import com.shield.module.utility.dto.ElectricityReadingResponse;
import com.shield.module.utility.dto.WaterTankCreateRequest;
import com.shield.module.utility.dto.WaterTankResponse;
import com.shield.module.utility.entity.ElectricityMeterEntity;
import com.shield.module.utility.entity.ElectricityReadingEntity;
import com.shield.module.utility.entity.WaterTankEntity;
import com.shield.module.utility.repository.ElectricityMeterRepository;
import com.shield.module.utility.repository.ElectricityReadingRepository;
import com.shield.module.utility.repository.WaterLevelLogRepository;
import com.shield.module.utility.repository.WaterTankRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UtilityServiceTest {

    @Mock
    private WaterTankRepository waterTankRepository;

    @Mock
    private WaterLevelLogRepository waterLevelLogRepository;

    @Mock
    private ElectricityMeterRepository electricityMeterRepository;

    @Mock
    private ElectricityReadingRepository electricityReadingRepository;

    @Mock
    private AuditLogService auditLogService;

    private UtilityService utilityService;

    @BeforeEach
    void setUp() {
        utilityService = new UtilityService(
                waterTankRepository,
                waterLevelLogRepository,
                electricityMeterRepository,
                electricityReadingRepository,
                auditLogService);
    }

    @Test
    void createWaterTankShouldSetTenantFromPrincipal() {
        when(waterTankRepository.save(any(WaterTankEntity.class))).thenAnswer(invocation -> {
            WaterTankEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        WaterTankCreateRequest request = new WaterTankCreateRequest(
                "OH Tank",
                "OVERHEAD",
                BigDecimal.valueOf(50000),
                "Block A Terrace");

        WaterTankResponse response = utilityService.createWaterTank(request, principal);

        assertEquals(principal.tenantId(), response.tenantId());
        assertEquals("OH Tank", response.tankName());
    }

    @Test
    void createElectricityReadingShouldSetRecorderFromPrincipal() {
        UUID meterId = UUID.randomUUID();

        ElectricityMeterEntity meter = new ElectricityMeterEntity();
        meter.setId(meterId);
        when(electricityMeterRepository.findByIdAndDeletedFalse(meterId)).thenReturn(Optional.of(meter));

        when(electricityReadingRepository.save(any(ElectricityReadingEntity.class))).thenAnswer(invocation -> {
            ElectricityReadingEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "committee@shield.dev", "COMMITTEE");
        ElectricityReadingCreateRequest request = new ElectricityReadingCreateRequest(
                meterId,
                LocalDate.of(2026, 2, 17),
                BigDecimal.valueOf(10500),
                BigDecimal.valueOf(220),
                BigDecimal.valueOf(1760));

        ElectricityReadingResponse response = utilityService.createElectricityReading(request, principal);

        assertEquals(principal.userId(), response.recordedBy());
        assertEquals(meterId, response.meterId());
    }
}
