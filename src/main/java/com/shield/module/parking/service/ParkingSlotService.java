package com.shield.module.parking.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.parking.dto.ParkingSlotAllocateRequest;
import com.shield.module.parking.dto.ParkingSlotCreateRequest;
import com.shield.module.parking.dto.ParkingSlotResponse;
import com.shield.module.parking.dto.ParkingSlotUpdateRequest;
import com.shield.module.parking.entity.ParkingSlotEntity;
import com.shield.module.parking.repository.ParkingSlotRepository;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParkingSlotService {

    private static final String ENTITY_PARKING_SLOT = "parking_slot";

    private final ParkingSlotRepository parkingSlotRepository;
    private final UnitRepository unitRepository;
    private final AuditLogService auditLogService;

    public ParkingSlotService(
            ParkingSlotRepository parkingSlotRepository,
            UnitRepository unitRepository,
            AuditLogService auditLogService) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.unitRepository = unitRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ParkingSlotResponse> list(Pageable pageable) {
        return PagedResponse.from(parkingSlotRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ParkingSlotResponse getById(UUID id) {
        return toResponse(findEntity(id));
    }

    public ParkingSlotResponse create(ParkingSlotCreateRequest request, ShieldPrincipal principal) {
        ensureUniqueSlotNumber(request.slotNumber(), null);

        ParkingSlotEntity entity = new ParkingSlotEntity();
        entity.setTenantId(principal.tenantId());
        entity.setSlotNumber(request.slotNumber().trim().toUpperCase());
        entity.setParkingType(request.parkingType());
        entity.setVehicleType(request.vehicleType());
        applyUnitAllocation(entity, request.unitId());

        ParkingSlotEntity saved = parkingSlotRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PARKING_SLOT_CREATED", ENTITY_PARKING_SLOT, saved.getId(), null);
        return toResponse(saved);
    }

    public ParkingSlotResponse update(UUID id, ParkingSlotUpdateRequest request, ShieldPrincipal principal) {
        ParkingSlotEntity entity = findEntity(id);
        ensureUniqueSlotNumber(request.slotNumber(), entity.getSlotNumber());

        entity.setSlotNumber(request.slotNumber().trim().toUpperCase());
        entity.setParkingType(request.parkingType());
        entity.setVehicleType(request.vehicleType());
        applyUnitAllocation(entity, request.unitId());

        ParkingSlotEntity saved = parkingSlotRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PARKING_SLOT_UPDATED", ENTITY_PARKING_SLOT, saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        ParkingSlotEntity entity = findEntity(id);
        if (entity.isAllocated()) {
            throw new BadRequestException("Allocated parking slot cannot be deleted");
        }

        entity.setDeleted(true);
        parkingSlotRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PARKING_SLOT_DELETED", ENTITY_PARKING_SLOT, entity.getId(), null);
    }

    public ParkingSlotResponse allocate(UUID id, ParkingSlotAllocateRequest request, ShieldPrincipal principal) {
        ParkingSlotEntity entity = findEntity(id);
        unitRepository.findByIdAndDeletedFalse(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.unitId()));

        if (entity.isAllocated()) {
            if (request.unitId().equals(entity.getUnitId())) {
                return toResponse(entity);
            }
            throw new BadRequestException("Parking slot is already allocated");
        }

        entity.setAllocated(true);
        entity.setUnitId(request.unitId());
        entity.setAllocatedAt(Instant.now());

        ParkingSlotEntity saved = parkingSlotRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PARKING_SLOT_ALLOCATED", ENTITY_PARKING_SLOT, saved.getId(), null);
        return toResponse(saved);
    }

    public ParkingSlotResponse deallocate(UUID id, ShieldPrincipal principal) {
        ParkingSlotEntity entity = findEntity(id);
        if (!entity.isAllocated()) {
            return toResponse(entity);
        }

        entity.setAllocated(false);
        entity.setUnitId(null);
        entity.setAllocatedAt(null);

        ParkingSlotEntity saved = parkingSlotRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PARKING_SLOT_DEALLOCATED", ENTITY_PARKING_SLOT, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ParkingSlotResponse> listAvailable(Pageable pageable) {
        return PagedResponse.from(parkingSlotRepository.findAllByAllocatedFalseAndDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ParkingSlotResponse> listByUnit(UUID unitId, Pageable pageable) {
        return PagedResponse.from(parkingSlotRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable).map(this::toResponse));
    }

    private void applyUnitAllocation(ParkingSlotEntity entity, UUID unitId) {
        if (unitId == null) {
            entity.setAllocated(false);
            entity.setUnitId(null);
            entity.setAllocatedAt(null);
            return;
        }

        unitRepository.findByIdAndDeletedFalse(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));

        entity.setAllocated(true);
        entity.setUnitId(unitId);
        if (entity.getAllocatedAt() == null) {
            entity.setAllocatedAt(Instant.now());
        }
    }

    private void ensureUniqueSlotNumber(String incomingSlotNumber, String existingSlotNumber) {
        String normalized = incomingSlotNumber.trim().toUpperCase();
        if (existingSlotNumber != null && normalized.equals(existingSlotNumber.trim().toUpperCase())) {
            return;
        }

        if (parkingSlotRepository.existsBySlotNumberIgnoreCaseAndDeletedFalse(normalized)) {
            throw new BadRequestException("Parking slot number already exists");
        }
    }

    private ParkingSlotEntity findEntity(UUID id) {
        return parkingSlotRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found: " + id));
    }

    private ParkingSlotResponse toResponse(ParkingSlotEntity entity) {
        return new ParkingSlotResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getSlotNumber(),
                entity.getParkingType(),
                entity.getVehicleType(),
                entity.getUnitId(),
                entity.isAllocated(),
                entity.getAllocatedAt());
    }
}
