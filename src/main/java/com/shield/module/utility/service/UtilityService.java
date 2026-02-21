package com.shield.module.utility.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.utility.dto.DieselGeneratorCreateRequest;
import com.shield.module.utility.dto.DieselGeneratorResponse;
import com.shield.module.utility.dto.DieselGeneratorUpdateRequest;
import com.shield.module.utility.dto.ElectricityConsumptionReportResponse;
import com.shield.module.utility.dto.ElectricityMeterCreateRequest;
import com.shield.module.utility.dto.ElectricityMeterResponse;
import com.shield.module.utility.dto.ElectricityMeterUpdateRequest;
import com.shield.module.utility.dto.ElectricityReadingCreateRequest;
import com.shield.module.utility.dto.ElectricityReadingResponse;
import com.shield.module.utility.dto.GeneratorLogCreateRequest;
import com.shield.module.utility.dto.GeneratorLogResponse;
import com.shield.module.utility.dto.GeneratorLogSummaryResponse;
import com.shield.module.utility.dto.GeneratorLogUpdateRequest;
import com.shield.module.utility.dto.WaterLevelLogCreateRequest;
import com.shield.module.utility.dto.WaterLevelLogResponse;
import com.shield.module.utility.dto.WaterTankCreateRequest;
import com.shield.module.utility.dto.WaterTankResponse;
import com.shield.module.utility.dto.WaterTankUpdateRequest;
import com.shield.module.utility.entity.DieselGeneratorEntity;
import com.shield.module.utility.entity.ElectricityMeterEntity;
import com.shield.module.utility.entity.ElectricityReadingEntity;
import com.shield.module.utility.entity.GeneratorLogEntity;
import com.shield.module.utility.entity.WaterLevelLogEntity;
import com.shield.module.utility.entity.WaterTankEntity;
import com.shield.module.utility.repository.DieselGeneratorRepository;
import com.shield.module.utility.repository.ElectricityMeterRepository;
import com.shield.module.utility.repository.ElectricityReadingRepository;
import com.shield.module.utility.repository.GeneratorLogRepository;
import com.shield.module.utility.repository.WaterLevelLogRepository;
import com.shield.module.utility.repository.WaterTankRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UtilityService {

    private static final String ENTITY_WATER_TANK = "water_tank";
    private static final String ENTITY_WATER_LEVEL_LOG = "water_level_log";
    private static final String ENTITY_ELECTRICITY_METER = "electricity_meter";
    private static final String ENTITY_ELECTRICITY_READING = "electricity_reading";
    private static final String ENTITY_DIESEL_GENERATOR = "diesel_generator";
    private static final String ENTITY_GENERATOR_LOG = "generator_log";

    private final WaterTankRepository waterTankRepository;
    private final WaterLevelLogRepository waterLevelLogRepository;
    private final ElectricityMeterRepository electricityMeterRepository;
    private final ElectricityReadingRepository electricityReadingRepository;
    private final DieselGeneratorRepository dieselGeneratorRepository;
    private final GeneratorLogRepository generatorLogRepository;
    private final AuditLogService auditLogService;

    public UtilityService(
            WaterTankRepository waterTankRepository,
            WaterLevelLogRepository waterLevelLogRepository,
            ElectricityMeterRepository electricityMeterRepository,
            ElectricityReadingRepository electricityReadingRepository,
            DieselGeneratorRepository dieselGeneratorRepository,
            GeneratorLogRepository generatorLogRepository,
            AuditLogService auditLogService) {
        this.waterTankRepository = waterTankRepository;
        this.waterLevelLogRepository = waterLevelLogRepository;
        this.electricityMeterRepository = electricityMeterRepository;
        this.electricityReadingRepository = electricityReadingRepository;
        this.dieselGeneratorRepository = dieselGeneratorRepository;
        this.generatorLogRepository = generatorLogRepository;
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "WATER_TANK_CREATED", ENTITY_WATER_TANK, saved.getId(), null);
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "WATER_TANK_UPDATED", ENTITY_WATER_TANK, saved.getId(), null);
        return toWaterTankResponse(saved);
    }

    public void deleteWaterTank(UUID id, ShieldPrincipal principal) {
        WaterTankEntity entity = waterTankRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water tank not found: " + id));

        entity.setDeleted(true);
        waterTankRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "WATER_TANK_DELETED", ENTITY_WATER_TANK, entity.getId(), null);
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "WATER_LEVEL_LOG_CREATED", ENTITY_WATER_LEVEL_LOG, saved.getId(), null);
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "ELECTRICITY_METER_CREATED", ENTITY_ELECTRICITY_METER, saved.getId(), null);
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
    public PagedResponse<ElectricityMeterResponse> listElectricityMetersByType(String meterType, Pageable pageable) {
        return PagedResponse.from(electricityMeterRepository.findAllByMeterTypeIgnoreCaseAndDeletedFalse(meterType, pageable)
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "ELECTRICITY_METER_UPDATED", ENTITY_ELECTRICITY_METER, saved.getId(), null);
        return toElectricityMeterResponse(saved);
    }

    public void deleteElectricityMeter(UUID id, ShieldPrincipal principal) {
        ElectricityMeterEntity entity = electricityMeterRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electricity meter not found: " + id));

        entity.setDeleted(true);
        electricityMeterRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "ELECTRICITY_METER_DELETED", ENTITY_ELECTRICITY_METER, entity.getId(), null);
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "ELECTRICITY_READING_CREATED", ENTITY_ELECTRICITY_READING, saved.getId(), null);
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

    @Transactional(readOnly = true)
    public PagedResponse<WaterLevelLogResponse> listWaterLevelLogsByDateRange(Instant from, Instant to, Pageable pageable) {
        validateInstantRange(from, to);
        return PagedResponse.from(waterLevelLogRepository.findAllByReadingTimeBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toWaterLevelLogResponse));
    }

    @Transactional(readOnly = true)
    public WaterLevelLogResponse getCurrentWaterLevelLog(UUID tankId) {
        WaterLevelLogEntity entity = tankId == null
                ? waterLevelLogRepository.findTopByDeletedFalseOrderByReadingTimeDesc()
                        .orElseThrow(() -> new ResourceNotFoundException("No water level logs found"))
                : waterLevelLogRepository.findTopByTankIdAndDeletedFalseOrderByReadingTimeDesc(tankId)
                        .orElseThrow(() -> new ResourceNotFoundException("No water level logs found for tank: " + tankId));
        return toWaterLevelLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ElectricityReadingResponse> listElectricityReadingsByDateRange(
            LocalDate from,
            LocalDate to,
            UUID meterId,
            Pageable pageable) {
        validateLocalDateRange(from, to);
        if (meterId != null) {
            return PagedResponse.from(electricityReadingRepository
                    .findAllByMeterIdAndReadingDateBetweenAndDeletedFalse(meterId, from, to, pageable)
                    .map(this::toElectricityReadingResponse));
        }
        return PagedResponse.from(electricityReadingRepository.findAllByReadingDateBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toElectricityReadingResponse));
    }

    @Transactional(readOnly = true)
    public ElectricityConsumptionReportResponse getElectricityConsumptionReport(LocalDate from, LocalDate to, UUID meterId) {
        validateLocalDateRange(from, to);

        List<ElectricityReadingEntity> rows = meterId == null
                ? electricityReadingRepository.findAllByReadingDateBetweenAndDeletedFalse(from, to)
                : electricityReadingRepository.findAllByMeterIdAndReadingDateBetweenAndDeletedFalse(meterId, from, to);

        BigDecimal totalUnits = sumScale2(rows.stream().map(ElectricityReadingEntity::getUnitsConsumed).toList());
        BigDecimal totalCost = sumScale2(rows.stream().map(ElectricityReadingEntity::getCost).toList());

        return new ElectricityConsumptionReportResponse(
                meterId,
                from,
                to,
                rows.size(),
                totalUnits,
                totalCost);
    }

    public DieselGeneratorResponse createDieselGenerator(DieselGeneratorCreateRequest request, ShieldPrincipal principal) {
        DieselGeneratorEntity entity = new DieselGeneratorEntity();
        entity.setTenantId(principal.tenantId());
        entity.setGeneratorName(request.generatorName());
        entity.setCapacityKva(request.capacityKva());
        entity.setLocation(request.location());

        DieselGeneratorEntity saved = dieselGeneratorRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DIESEL_GENERATOR_CREATED", ENTITY_DIESEL_GENERATOR, saved.getId(), null);
        return toDieselGeneratorResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DieselGeneratorResponse> listDieselGenerators(Pageable pageable) {
        return PagedResponse.from(dieselGeneratorRepository.findAllByDeletedFalse(pageable).map(this::toDieselGeneratorResponse));
    }

    @Transactional(readOnly = true)
    public DieselGeneratorResponse getDieselGenerator(UUID id) {
        DieselGeneratorEntity entity = dieselGeneratorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diesel generator not found: " + id));
        return toDieselGeneratorResponse(entity);
    }

    public DieselGeneratorResponse updateDieselGenerator(UUID id, DieselGeneratorUpdateRequest request, ShieldPrincipal principal) {
        DieselGeneratorEntity entity = dieselGeneratorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diesel generator not found: " + id));

        entity.setGeneratorName(request.generatorName());
        entity.setCapacityKva(request.capacityKva());
        entity.setLocation(request.location());

        DieselGeneratorEntity saved = dieselGeneratorRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DIESEL_GENERATOR_UPDATED", ENTITY_DIESEL_GENERATOR, saved.getId(), null);
        return toDieselGeneratorResponse(saved);
    }

    public void deleteDieselGenerator(UUID id, ShieldPrincipal principal) {
        DieselGeneratorEntity entity = dieselGeneratorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diesel generator not found: " + id));

        entity.setDeleted(true);
        dieselGeneratorRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DIESEL_GENERATOR_DELETED", ENTITY_DIESEL_GENERATOR, entity.getId(), null);
    }

    public GeneratorLogResponse createGeneratorLog(GeneratorLogCreateRequest request, ShieldPrincipal principal) {
        dieselGeneratorRepository.findByIdAndDeletedFalse(request.generatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Diesel generator not found: " + request.generatorId()));

        GeneratorLogEntity entity = new GeneratorLogEntity();
        entity.setTenantId(principal.tenantId());
        entity.setGeneratorId(request.generatorId());
        entity.setLogDate(request.logDate());
        entity.setStartTime(request.startTime());
        entity.setStopTime(request.stopTime());
        entity.setRuntimeHours(request.runtimeHours());
        entity.setDieselConsumed(request.dieselConsumed());
        entity.setDieselCost(request.dieselCost());
        entity.setMeterReadingBefore(request.meterReadingBefore());
        entity.setMeterReadingAfter(request.meterReadingAfter());
        entity.setUnitsGenerated(request.unitsGenerated());
        entity.setOperatorId(request.operatorId() != null ? request.operatorId() : principal.userId());

        GeneratorLogEntity saved = generatorLogRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "GENERATOR_LOG_CREATED", ENTITY_GENERATOR_LOG, saved.getId(), null);
        return toGeneratorLogResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<GeneratorLogResponse> listGeneratorLogs(Pageable pageable) {
        return PagedResponse.from(generatorLogRepository.findAllByDeletedFalse(pageable).map(this::toGeneratorLogResponse));
    }

    @Transactional(readOnly = true)
    public GeneratorLogResponse getGeneratorLog(UUID id) {
        GeneratorLogEntity entity = generatorLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generator log not found: " + id));
        return toGeneratorLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<GeneratorLogResponse> listGeneratorLogsByGenerator(UUID generatorId, Pageable pageable) {
        return PagedResponse.from(generatorLogRepository.findAllByGeneratorIdAndDeletedFalse(generatorId, pageable)
                .map(this::toGeneratorLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<GeneratorLogResponse> listGeneratorLogsByDateRange(
            LocalDate from,
            LocalDate to,
            UUID generatorId,
            Pageable pageable) {
        validateLocalDateRange(from, to);
        if (generatorId != null) {
            return PagedResponse.from(generatorLogRepository
                    .findAllByGeneratorIdAndLogDateBetweenAndDeletedFalse(generatorId, from, to, pageable)
                    .map(this::toGeneratorLogResponse));
        }
        return PagedResponse.from(generatorLogRepository.findAllByLogDateBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toGeneratorLogResponse));
    }

    @Transactional(readOnly = true)
    public GeneratorLogSummaryResponse getGeneratorLogSummary(LocalDate from, LocalDate to, UUID generatorId) {
        validateLocalDateRange(from, to);

        List<GeneratorLogEntity> rows = generatorId == null
                ? generatorLogRepository.findAllByLogDateBetweenAndDeletedFalse(from, to)
                : generatorLogRepository.findAllByGeneratorIdAndLogDateBetweenAndDeletedFalse(generatorId, from, to);

        return new GeneratorLogSummaryResponse(
                generatorId,
                from,
                to,
                rows.size(),
                sumScale2(rows.stream().map(GeneratorLogEntity::getRuntimeHours).toList()),
                sumScale2(rows.stream().map(GeneratorLogEntity::getDieselConsumed).toList()),
                sumScale2(rows.stream().map(GeneratorLogEntity::getDieselCost).toList()),
                sumScale2(rows.stream().map(GeneratorLogEntity::getUnitsGenerated).toList()));
    }

    public GeneratorLogResponse updateGeneratorLog(UUID id, GeneratorLogUpdateRequest request, ShieldPrincipal principal) {
        GeneratorLogEntity entity = generatorLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generator log not found: " + id));

        entity.setLogDate(request.logDate());
        entity.setStartTime(request.startTime());
        entity.setStopTime(request.stopTime());
        entity.setRuntimeHours(request.runtimeHours());
        entity.setDieselConsumed(request.dieselConsumed());
        entity.setDieselCost(request.dieselCost());
        entity.setMeterReadingBefore(request.meterReadingBefore());
        entity.setMeterReadingAfter(request.meterReadingAfter());
        entity.setUnitsGenerated(request.unitsGenerated());
        if (request.operatorId() != null) {
            entity.setOperatorId(request.operatorId());
        }

        GeneratorLogEntity saved = generatorLogRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "GENERATOR_LOG_UPDATED", ENTITY_GENERATOR_LOG, saved.getId(), null);
        return toGeneratorLogResponse(saved);
    }

    public void deleteGeneratorLog(UUID id, ShieldPrincipal principal) {
        GeneratorLogEntity entity = generatorLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generator log not found: " + id));

        entity.setDeleted(true);
        generatorLogRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "GENERATOR_LOG_DELETED", ENTITY_GENERATOR_LOG, entity.getId(), null);
    }

    private void validateInstantRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both from and to date-time values are required");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("to date-time cannot be before from date-time");
        }
    }

    private void validateLocalDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both from and to dates are required");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("to date cannot be before from date");
        }
    }

    private BigDecimal sumScale2(List<BigDecimal> values) {
        return values.stream()
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
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

    private DieselGeneratorResponse toDieselGeneratorResponse(DieselGeneratorEntity entity) {
        return new DieselGeneratorResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getGeneratorName(),
                entity.getCapacityKva(),
                entity.getLocation());
    }

    private GeneratorLogResponse toGeneratorLogResponse(GeneratorLogEntity entity) {
        return new GeneratorLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getGeneratorId(),
                entity.getLogDate(),
                entity.getStartTime(),
                entity.getStopTime(),
                entity.getRuntimeHours(),
                entity.getDieselConsumed(),
                entity.getDieselCost(),
                entity.getMeterReadingBefore(),
                entity.getMeterReadingAfter(),
                entity.getUnitsGenerated(),
                entity.getOperatorId());
    }
}
