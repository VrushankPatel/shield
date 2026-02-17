package com.shield.module.utility.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.utility.dto.ElectricityMeterCreateRequest;
import com.shield.module.utility.dto.ElectricityMeterResponse;
import com.shield.module.utility.dto.ElectricityMeterUpdateRequest;
import com.shield.module.utility.dto.ElectricityReadingCreateRequest;
import com.shield.module.utility.dto.ElectricityReadingResponse;
import com.shield.module.utility.dto.WaterLevelLogCreateRequest;
import com.shield.module.utility.dto.WaterLevelLogResponse;
import com.shield.module.utility.dto.WaterTankCreateRequest;
import com.shield.module.utility.dto.WaterTankResponse;
import com.shield.module.utility.dto.WaterTankUpdateRequest;
import com.shield.module.utility.entity.ElectricityMeterEntity;
import com.shield.module.utility.entity.ElectricityReadingEntity;
import com.shield.module.utility.entity.WaterLevelLogEntity;
import com.shield.module.utility.entity.WaterTankEntity;
import com.shield.module.utility.repository.ElectricityMeterRepository;
import com.shield.module.utility.repository.ElectricityReadingRepository;
import com.shield.module.utility.repository.WaterLevelLogRepository;
import com.shield.module.utility.repository.WaterTankRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UtilityService {

    private final WaterTankRepository waterTankRepository;
    private final WaterLevelLogRepository waterLevelLogRepository;
    private final ElectricityMeterRepository electricityMeterRepository;
    private final ElectricityReadingRepository electricityReadingRepository;
    private final AuditLogService auditLogService;

    public UtilityService(
            WaterTankRepository waterTankRepository,
            WaterLevelLogRepository waterLevelLogRepository,
            ElectricityMeterRepository electricityMeterRepository,
            ElectricityReadingRepository electricityReadingRepository,
            AuditLogService auditLogService) {
        this.waterTankRepository = waterTankRepository;
        this.waterLevelLogRepository = waterLevelLogRepository;
        this.electricityMeterRepository = electricityMeterRepository;
        this.electricityReadingRepository = electricityReadingRepository;
        this.auditLogService = auditLogService;
    }

    public WaterTankResponse createWaterTank(WaterTankCreateRequest request, ShieldPrincipal principal) {
        WaterTankEntity entity = new WaterTankEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTankName(request.tankName());
        entity.setTankType(request.tankType());
        entity.setCapacity(request.capacity());
        entity.setLocation(request.location());

        WaterTankEntity saved = waterTankRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "WATER_TANK_CREATED", "water_tank", saved.getId(), null);
        return toWaterTankResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WaterTankResponse> listWaterTanks(Pageable pageable) {
        return PagedResponse.from(waterTankRepository.findAllByDeletedFalse(pageable).map(this::toWaterTankResponse));
    }

    @Transactional(readOnly = true)
    public WaterTankResponse getWaterTank(UUID id) {
        WaterTankEntity entity = waterTankRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water tank not found: " + id));
        return toWaterTankResponse(entity);
    }

    public WaterTankResponse updateWaterTank(UUID id, WaterTankUpdateRequest request, ShieldPrincipal principal) {
        WaterTankEntity entity = waterTankRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water tank not found: " + id));

        entity.setTankName(request.tankName());
        entity.setTankType(request.tankType());
        entity.setCapacity(request.capacity());
        entity.setLocation(request.location());

        WaterTankEntity saved = waterTankRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "WATER_TANK_UPDATED", "water_tank", saved.getId(), null);
        return toWaterTankResponse(saved);
    }

    public void deleteWaterTank(UUID id, ShieldPrincipal principal) {
        WaterTankEntity entity = waterTankRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water tank not found: " + id));

        entity.setDeleted(true);
        waterTankRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "WATER_TANK_DELETED", "water_tank", entity.getId(), null);
    }

    public WaterLevelLogResponse createWaterLevelLog(WaterLevelLogCreateRequest request, ShieldPrincipal principal) {
        waterTankRepository.findByIdAndDeletedFalse(request.tankId())
                .orElseThrow(() -> new ResourceNotFoundException("Water tank not found: " + request.tankId()));

        WaterLevelLogEntity entity = new WaterLevelLogEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTankId(request.tankId());
        entity.setReadingTime(request.readingTime() != null ? request.readingTime() : Instant.now());
        entity.setLevelPercentage(request.levelPercentage());
        entity.setVolume(request.volume());
        entity.setRecordedBy(principal.userId());

        WaterLevelLogEntity saved = waterLevelLogRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "WATER_LEVEL_LOG_CREATED", "water_level_log", saved.getId(), null);
        return toWaterLevelLogResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WaterLevelLogResponse> listWaterLevelLogs(Pageable pageable) {
        return PagedResponse.from(waterLevelLogRepository.findAllByDeletedFalse(pageable).map(this::toWaterLevelLogResponse));
    }

    @Transactional(readOnly = true)
    public WaterLevelLogResponse getWaterLevelLog(UUID id) {
        WaterLevelLogEntity entity = waterLevelLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water level log not found: " + id));
        return toWaterLevelLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WaterLevelLogResponse> listWaterLevelLogsByTank(UUID tankId, Pageable pageable) {
        return PagedResponse.from(waterLevelLogRepository.findAllByTankIdAndDeletedFalse(tankId, pageable)
                .map(this::toWaterLevelLogResponse));
    }

    public ElectricityMeterResponse createElectricityMeter(ElectricityMeterCreateRequest request, ShieldPrincipal principal) {
        ElectricityMeterEntity entity = new ElectricityMeterEntity();
        entity.setTenantId(principal.tenantId());
        entity.setMeterNumber(request.meterNumber());
        entity.setMeterType(request.meterType());
        entity.setLocation(request.location());
        entity.setUnitId(request.unitId());

        ElectricityMeterEntity saved = electricityMeterRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ELECTRICITY_METER_CREATED", "electricity_meter", saved.getId(), null);
        return toElectricityMeterResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ElectricityMeterResponse> listElectricityMeters(Pageable pageable) {
        return PagedResponse.from(electricityMeterRepository.findAllByDeletedFalse(pageable).map(this::toElectricityMeterResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ElectricityMeterResponse> listElectricityMetersByUnit(UUID unitId, Pageable pageable) {
        return PagedResponse.from(electricityMeterRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable)
                .map(this::toElectricityMeterResponse));
    }

    @Transactional(readOnly = true)
    public ElectricityMeterResponse getElectricityMeter(UUID id) {
        ElectricityMeterEntity entity = electricityMeterRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electricity meter not found: " + id));
        return toElectricityMeterResponse(entity);
    }

    public ElectricityMeterResponse updateElectricityMeter(UUID id, ElectricityMeterUpdateRequest request, ShieldPrincipal principal) {
        ElectricityMeterEntity entity = electricityMeterRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electricity meter not found: " + id));

        entity.setMeterType(request.meterType());
        entity.setLocation(request.location());
        entity.setUnitId(request.unitId());

        ElectricityMeterEntity saved = electricityMeterRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ELECTRICITY_METER_UPDATED", "electricity_meter", saved.getId(), null);
        return toElectricityMeterResponse(saved);
    }

    public void deleteElectricityMeter(UUID id, ShieldPrincipal principal) {
        ElectricityMeterEntity entity = electricityMeterRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electricity meter not found: " + id));

        entity.setDeleted(true);
        electricityMeterRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ELECTRICITY_METER_DELETED", "electricity_meter", entity.getId(), null);
    }

    public ElectricityReadingResponse createElectricityReading(ElectricityReadingCreateRequest request, ShieldPrincipal principal) {
        electricityMeterRepository.findByIdAndDeletedFalse(request.meterId())
                .orElseThrow(() -> new ResourceNotFoundException("Electricity meter not found: " + request.meterId()));

        ElectricityReadingEntity entity = new ElectricityReadingEntity();
        entity.setTenantId(principal.tenantId());
        entity.setMeterId(request.meterId());
        entity.setReadingDate(request.readingDate());
        entity.setReadingValue(request.readingValue());
        entity.setUnitsConsumed(request.unitsConsumed());
        entity.setCost(request.cost());
        entity.setRecordedBy(principal.userId());

        ElectricityReadingEntity saved = electricityReadingRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ELECTRICITY_READING_CREATED", "electricity_reading", saved.getId(), null);
        return toElectricityReadingResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ElectricityReadingResponse> listElectricityReadings(Pageable pageable) {
        return PagedResponse.from(electricityReadingRepository.findAllByDeletedFalse(pageable).map(this::toElectricityReadingResponse));
    }

    @Transactional(readOnly = true)
    public ElectricityReadingResponse getElectricityReading(UUID id) {
        ElectricityReadingEntity entity = electricityReadingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electricity reading not found: " + id));
        return toElectricityReadingResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ElectricityReadingResponse> listElectricityReadingsByMeter(UUID meterId, Pageable pageable) {
        return PagedResponse.from(electricityReadingRepository.findAllByMeterIdAndDeletedFalse(meterId, pageable)
                .map(this::toElectricityReadingResponse));
    }

    private WaterTankResponse toWaterTankResponse(WaterTankEntity entity) {
        return new WaterTankResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTankName(),
                entity.getTankType(),
                entity.getCapacity(),
                entity.getLocation());
    }

    private WaterLevelLogResponse toWaterLevelLogResponse(WaterLevelLogEntity entity) {
        return new WaterLevelLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTankId(),
                entity.getReadingTime(),
                entity.getLevelPercentage(),
                entity.getVolume(),
                entity.getRecordedBy());
    }

    private ElectricityMeterResponse toElectricityMeterResponse(ElectricityMeterEntity entity) {
        return new ElectricityMeterResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeterNumber(),
                entity.getMeterType(),
                entity.getLocation(),
                entity.getUnitId());
    }

    private ElectricityReadingResponse toElectricityReadingResponse(ElectricityReadingEntity entity) {
        return new ElectricityReadingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeterId(),
                entity.getReadingDate(),
                entity.getReadingValue(),
                entity.getUnitsConsumed(),
                entity.getCost(),
                entity.getRecordedBy());
    }
}
