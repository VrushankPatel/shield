package com.shield.module.utility.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.utility.dto.ElectricityConsumptionReportResponse;
import com.shield.module.utility.dto.ElectricityReadingCreateRequest;
import com.shield.module.utility.dto.ElectricityReadingResponse;
import com.shield.module.utility.dto.WaterLevelLogCreateRequest;
import com.shield.module.utility.dto.WaterLevelLogResponse;
import com.shield.module.utility.dto.WaterTankCreateRequest;
import com.shield.module.utility.dto.WaterTankResponse;
import com.shield.module.utility.entity.ElectricityMeterEntity;
import com.shield.module.utility.entity.ElectricityReadingEntity;
import com.shield.module.utility.entity.WaterLevelLogEntity;
import com.shield.module.utility.entity.WaterTankEntity;
import com.shield.module.utility.repository.ElectricityMeterRepository;
import com.shield.module.utility.repository.ElectricityReadingRepository;
import com.shield.module.utility.repository.WaterLevelLogRepository;
import com.shield.module.utility.repository.WaterTankRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    @Test
    void getElectricityConsumptionReportShouldAggregateUnitsAndCost() {
        UUID meterId = UUID.randomUUID();

        ElectricityReadingEntity r1 = new ElectricityReadingEntity();
        r1.setMeterId(meterId);
        r1.setUnitsConsumed(BigDecimal.valueOf(120.5));
        r1.setCost(BigDecimal.valueOf(964.5));

        ElectricityReadingEntity r2 = new ElectricityReadingEntity();
        r2.setMeterId(meterId);
        r2.setUnitsConsumed(BigDecimal.valueOf(79.5));
        r2.setCost(BigDecimal.valueOf(635.5));

        when(electricityReadingRepository.findAllByMeterIdAndReadingDateBetweenAndDeletedFalse(
                meterId,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)))
                .thenReturn(List.of(r1, r2));

        ElectricityConsumptionReportResponse report = utilityService.getElectricityConsumptionReport(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                meterId);

        assertEquals(2, report.totalReadings());
        assertEquals(BigDecimal.valueOf(200.00).setScale(2), report.totalUnitsConsumed());
        assertEquals(BigDecimal.valueOf(1600.00).setScale(2), report.totalCost());
    }

    @Test
    void createWaterLevelLogShouldSetNowWhenReadingTimeMissing() {
        UUID tankId = UUID.randomUUID();
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");

        WaterTankEntity tank = new WaterTankEntity();
        tank.setId(tankId);
        when(waterTankRepository.findByIdAndDeletedFalse(tankId)).thenReturn(Optional.of(tank));

        when(waterLevelLogRepository.save(any(WaterLevelLogEntity.class))).thenAnswer(invocation -> {
            WaterLevelLogEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        WaterLevelLogResponse response = utilityService.createWaterLevelLog(new WaterLevelLogCreateRequest(
                tankId,
                null,
                BigDecimal.valueOf(75),
                BigDecimal.valueOf(30000)), principal);

        assertEquals(principal.userId(), response.recordedBy());
        assertEquals(tankId, response.tankId());
    }

    @Test
    void createWaterLevelLogShouldThrowWhenTankMissing() {
        UUID tankId = UUID.randomUUID();
        when(waterTankRepository.findByIdAndDeletedFalse(tankId)).thenReturn(Optional.empty());

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");

        assertThrows(ResourceNotFoundException.class, () -> utilityService.createWaterLevelLog(new WaterLevelLogCreateRequest(
                tankId,
                Instant.now(),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(25000)), principal));
    }

    @Test
    void listWaterLevelLogsByDateRangeShouldValidateRange() {
        assertThrows(BadRequestException.class, () -> utilityService.listWaterLevelLogsByDateRange(
                Instant.parse("2026-02-20T10:00:00Z"),
                Instant.parse("2026-02-19T10:00:00Z"),
                Pageable.ofSize(10)));
    }

    @Test
    void getCurrentWaterLevelLogShouldUseGlobalLatestWhenTankNotProvided() {
        WaterLevelLogEntity log = new WaterLevelLogEntity();
        log.setId(UUID.randomUUID());
        log.setTenantId(UUID.randomUUID());
        log.setTankId(UUID.randomUUID());
        log.setReadingTime(Instant.now());

        when(waterLevelLogRepository.findTopByDeletedFalseOrderByReadingTimeDesc()).thenReturn(Optional.of(log));

        WaterLevelLogResponse response = utilityService.getCurrentWaterLevelLog(null);
        assertEquals(log.getId(), response.id());
    }

    @Test
    void listElectricityReadingsByDateRangeShouldValidateDates() {
        assertThrows(BadRequestException.class, () -> utilityService.listElectricityReadingsByDateRange(
                null,
                LocalDate.now(),
                null,
                Pageable.ofSize(10)));
    }

    @Test
    void getElectricityConsumptionReportShouldUseAllMetersWhenMeterMissing() {
        ElectricityReadingEntity r1 = new ElectricityReadingEntity();
        r1.setUnitsConsumed(BigDecimal.valueOf(10));
        r1.setCost(BigDecimal.valueOf(50));

        ElectricityReadingEntity r2 = new ElectricityReadingEntity();
        r2.setUnitsConsumed(null);
        r2.setCost(null);

        when(electricityReadingRepository.findAllByReadingDateBetweenAndDeletedFalse(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 10))).thenReturn(List.of(r1, r2));

        ElectricityConsumptionReportResponse report = utilityService.getElectricityConsumptionReport(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 10),
                null);

        assertEquals(2, report.totalReadings());
        assertEquals(BigDecimal.valueOf(10.00).setScale(2), report.totalUnitsConsumed());
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), report.totalCost());
    }

    @Test
    void listElectricityMetersByTypeShouldReturnMappedPage() {
        ElectricityMeterEntity meter = new ElectricityMeterEntity();
        meter.setId(UUID.randomUUID());
        meter.setMeterNumber("MTR-001");
        meter.setMeterType("MAIN");

        when(electricityMeterRepository.findAllByMeterTypeIgnoreCaseAndDeletedFalse("MAIN", Pageable.ofSize(5)))
                .thenReturn(new PageImpl<>(List.of(meter)));

        assertEquals(1, utilityService.listElectricityMetersByType("MAIN", Pageable.ofSize(5)).content().size());
    }

    @Test
    void createElectricityReadingShouldThrowWhenMeterMissing() {
        UUID meterId = UUID.randomUUID();
        when(electricityMeterRepository.findByIdAndDeletedFalse(meterId)).thenReturn(Optional.empty());

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "committee@shield.dev", "COMMITTEE");
        ElectricityReadingCreateRequest request = new ElectricityReadingCreateRequest(
                meterId,
                LocalDate.of(2026, 2, 17),
                BigDecimal.valueOf(10500),
                BigDecimal.valueOf(220),
                BigDecimal.valueOf(1760));

        assertThrows(ResourceNotFoundException.class, () -> utilityService.createElectricityReading(request, principal));
    }

    @Test
    void getCurrentWaterLevelLogShouldUseTankSpecificLookup() {
        UUID tankId = UUID.randomUUID();
        WaterLevelLogEntity log = new WaterLevelLogEntity();
        log.setId(UUID.randomUUID());
        log.setTenantId(UUID.randomUUID());
        log.setTankId(tankId);
        log.setReadingTime(Instant.now());

        when(waterLevelLogRepository.findTopByTankIdAndDeletedFalseOrderByReadingTimeDesc(tankId)).thenReturn(Optional.of(log));

        WaterLevelLogResponse response = utilityService.getCurrentWaterLevelLog(tankId);
        assertEquals(tankId, response.tankId());
    }
}
