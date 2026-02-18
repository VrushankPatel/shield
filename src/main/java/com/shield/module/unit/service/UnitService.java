package com.shield.module.unit.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.move.entity.MoveRecordEntity;
import com.shield.module.move.repository.MoveRecordRepository;
import com.shield.module.unit.dto.UnitCreateRequest;
import com.shield.module.unit.dto.UnitHistoryResponse;
import com.shield.module.unit.dto.UnitResponse;
import com.shield.module.unit.dto.UnitUpdateRequest;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.unit.entity.UnitStatus;
import com.shield.module.unit.mapper.UnitMapper;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.dto.UserResponse;
import com.shield.module.user.mapper.UserMapper;
import com.shield.module.user.repository.UserRepository;
import com.shield.tenant.context.TenantContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MoveRecordRepository moveRecordRepository;
    private final AuditLogService auditLogService;

    public UnitResponse create(UnitCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        UnitEntity unit = new UnitEntity();
        unit.setTenantId(tenantId);
        unit.setUnitNumber(request.unitNumber());
        unit.setBlock(request.block());
        unit.setType(request.type());
        unit.setSquareFeet(request.squareFeet());
        unit.setStatus(request.status());

        UnitEntity saved = unitRepository.save(unit);
        auditLogService.record(tenantId, null, "UNIT_CREATED", "unit", saved.getId(), null);
        return unitMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UnitResponse> list(Pageable pageable) {
        return PagedResponse.from(unitRepository.findAllByDeletedFalse(pageable).map(unitMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UnitResponse> listByBlock(String block, Pageable pageable) {
        return PagedResponse.from(unitRepository.findAllByBlockIgnoreCaseAndDeletedFalse(block, pageable).map(unitMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UnitResponse> listAvailable(Pageable pageable) {
        return PagedResponse.from(unitRepository.findAllByStatusAndDeletedFalse(UnitStatus.VACANT, pageable).map(unitMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UnitResponse getById(UUID id) {
        UnitEntity unit = unitRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));
        return unitMapper.toResponse(unit);
    }

    public UnitResponse update(UUID id, UnitUpdateRequest request) {
        UnitEntity unit = unitRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));

        unit.setUnitNumber(request.unitNumber());
        unit.setBlock(request.block());
        unit.setType(request.type());
        unit.setSquareFeet(request.squareFeet());
        unit.setStatus(request.status());

        UnitEntity saved = unitRepository.save(unit);
        auditLogService.record(saved.getTenantId(), null, "UNIT_UPDATED", "unit", saved.getId(), null);
        return unitMapper.toResponse(saved);
    }

    public void delete(UUID id) {
        UnitEntity unit = unitRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));

        unit.setDeleted(true);
        unitRepository.save(unit);
        auditLogService.record(unit.getTenantId(), null, "UNIT_DELETED", "unit", unit.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listMembers(UUID unitId, Pageable pageable) {
        unitRepository.findByIdAndDeletedFalse(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
        return PagedResponse.from(userRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable).map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UnitHistoryResponse> listHistory(UUID unitId, Pageable pageable) {
        unitRepository.findByIdAndDeletedFalse(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
        return PagedResponse.from(moveRecordRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable).map(this::toHistoryResponse));
    }

    private UnitHistoryResponse toHistoryResponse(MoveRecordEntity entity) {
        return new UnitHistoryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUnitId(),
                entity.getUserId(),
                entity.getMoveType(),
                entity.getStatus(),
                entity.getEffectiveDate(),
                entity.getApprovalDate(),
                entity.getDecisionNotes(),
                entity.getCreatedAt());
    }
}
