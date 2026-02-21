package com.shield.module.asset.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
import com.shield.module.asset.dto.PreventiveMaintenanceCreateRequest;
import com.shield.module.asset.dto.PreventiveMaintenanceResponse;
import com.shield.module.asset.dto.PreventiveMaintenanceUpdateRequest;
import com.shield.module.asset.entity.MaintenanceFrequency;
import com.shield.module.asset.entity.PreventiveMaintenanceScheduleEntity;
import com.shield.module.asset.repository.AssetRepository;
import com.shield.module.asset.repository.PreventiveMaintenanceScheduleRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PreventiveMaintenanceService {

    private static final String ENTITY_PREVENTIVE_MAINTENANCE_SCHEDULE = "preventive_maintenance_schedule";

    private final PreventiveMaintenanceScheduleRepository preventiveMaintenanceScheduleRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    public PreventiveMaintenanceResponse create(PreventiveMaintenanceCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        assetRepository.findByIdAndDeletedFalse(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.assetId()));

        PreventiveMaintenanceScheduleEntity entity = new PreventiveMaintenanceScheduleEntity();
        entity.setTenantId(principal.tenantId());
        entity.setAssetId(request.assetId());
        entity.setMaintenanceType(request.maintenanceType().trim());
        entity.setFrequency(request.frequency());
        entity.setNextMaintenanceDate(request.nextMaintenanceDate());
        entity.setAssignedVendorId(request.assignedVendorId());
        entity.setActive(request.active() == null || request.active());

        PreventiveMaintenanceScheduleEntity saved = preventiveMaintenanceScheduleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PREVENTIVE_MAINTENANCE_CREATED", ENTITY_PREVENTIVE_MAINTENANCE_SCHEDULE, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PreventiveMaintenanceResponse> list(Pageable pageable) {
        return PagedResponse.from(preventiveMaintenanceScheduleRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PreventiveMaintenanceResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public PreventiveMaintenanceResponse update(UUID id, PreventiveMaintenanceUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        assetRepository.findByIdAndDeletedFalse(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.assetId()));

        PreventiveMaintenanceScheduleEntity entity = findById(id);
        entity.setAssetId(request.assetId());
        entity.setMaintenanceType(request.maintenanceType().trim());
        entity.setFrequency(request.frequency());
        entity.setLastMaintenanceDate(request.lastMaintenanceDate());
        entity.setNextMaintenanceDate(request.nextMaintenanceDate());
        entity.setAssignedVendorId(request.assignedVendorId());
        entity.setActive(request.active() == null || request.active());

        PreventiveMaintenanceScheduleEntity saved = preventiveMaintenanceScheduleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PREVENTIVE_MAINTENANCE_UPDATED", ENTITY_PREVENTIVE_MAINTENANCE_SCHEDULE, saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        PreventiveMaintenanceScheduleEntity entity = findById(id);
        entity.setDeleted(true);
        preventiveMaintenanceScheduleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PREVENTIVE_MAINTENANCE_DELETED", ENTITY_PREVENTIVE_MAINTENANCE_SCHEDULE, id, null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PreventiveMaintenanceResponse> listByAsset(UUID assetId, Pageable pageable) {
        return PagedResponse.from(preventiveMaintenanceScheduleRepository.findAllByAssetIdAndDeletedFalse(assetId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PreventiveMaintenanceResponse> listDue(Pageable pageable) {
        return PagedResponse.from(preventiveMaintenanceScheduleRepository
                .findAllByActiveTrueAndNextMaintenanceDateLessThanEqualAndDeletedFalse(LocalDate.now(), pageable)
                .map(this::toResponse));
    }

    public PreventiveMaintenanceResponse execute(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        PreventiveMaintenanceScheduleEntity entity = findById(id);
        LocalDate today = LocalDate.now();
        entity.setLastMaintenanceDate(today);
        entity.setNextMaintenanceDate(calculateNextDate(today, entity.getFrequency()));

        PreventiveMaintenanceScheduleEntity saved = preventiveMaintenanceScheduleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PREVENTIVE_MAINTENANCE_EXECUTED", ENTITY_PREVENTIVE_MAINTENANCE_SCHEDULE, id, null);
        return toResponse(saved);
    }

    private LocalDate calculateNextDate(LocalDate date, MaintenanceFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
        };
    }

    private PreventiveMaintenanceScheduleEntity findById(UUID id) {
        return preventiveMaintenanceScheduleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preventive maintenance schedule not found: " + id));
    }

    private PreventiveMaintenanceResponse toResponse(PreventiveMaintenanceScheduleEntity entity) {
        return new PreventiveMaintenanceResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAssetId(),
                entity.getMaintenanceType(),
                entity.getFrequency(),
                entity.getLastMaintenanceDate(),
                entity.getNextMaintenanceDate(),
                entity.getAssignedVendorId(),
                entity.isActive());
    }
}
