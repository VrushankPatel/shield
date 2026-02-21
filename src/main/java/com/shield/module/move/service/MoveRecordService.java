package com.shield.module.move.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.move.dto.MoveRecordCreateRequest;
import com.shield.module.move.dto.MoveRecordDecisionRequest;
import com.shield.module.move.dto.MoveRecordResponse;
import com.shield.module.move.entity.MoveRecordEntity;
import com.shield.module.move.entity.MoveStatus;
import com.shield.module.move.entity.MoveType;
import com.shield.module.move.repository.MoveRecordRepository;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MoveRecordService {

    private static final String ENTITY_MOVE_RECORD = "move_record";

    private final MoveRecordRepository moveRecordRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public MoveRecordService(
            MoveRecordRepository moveRecordRepository,
            UnitRepository unitRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.moveRecordRepository = moveRecordRepository;
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public MoveRecordResponse createMoveIn(MoveRecordCreateRequest request, ShieldPrincipal principal) {
        return create(request, MoveType.MOVE_IN, principal);
    }

    public MoveRecordResponse createMoveOut(MoveRecordCreateRequest request, ShieldPrincipal principal) {
        return create(request, MoveType.MOVE_OUT, principal);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MoveRecordResponse> list(Pageable pageable) {
        return PagedResponse.from(moveRecordRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MoveRecordResponse> listByUnit(UUID unitId, Pageable pageable) {
        return PagedResponse.from(moveRecordRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MoveRecordResponse> listPendingApprovals(Pageable pageable) {
        return PagedResponse.from(moveRecordRepository.findAllByStatusAndDeletedFalse(MoveStatus.PENDING, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public MoveRecordResponse getById(UUID id, ShieldPrincipal principal) {
        MoveRecordEntity entity = findEntity(id);
        enforceSelfOrPrivileged(principal, entity.getUserId());
        return toResponse(entity);
    }

    public MoveRecordResponse approve(UUID id, MoveRecordDecisionRequest request, ShieldPrincipal principal) {
        MoveRecordEntity entity = findEntity(id);
        entity.setStatus(MoveStatus.APPROVED);
        entity.setDecisionNotes(request.decisionNotes());
        entity.setApprovedBy(principal.userId());
        entity.setApprovalDate(LocalDate.now());

        MoveRecordEntity saved = moveRecordRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MOVE_RECORD_APPROVED", ENTITY_MOVE_RECORD, saved.getId(), null);
        return toResponse(saved);
    }

    public MoveRecordResponse reject(UUID id, MoveRecordDecisionRequest request, ShieldPrincipal principal) {
        MoveRecordEntity entity = findEntity(id);
        entity.setStatus(MoveStatus.REJECTED);
        entity.setDecisionNotes(request.decisionNotes());
        entity.setApprovedBy(principal.userId());
        entity.setApprovalDate(LocalDate.now());

        MoveRecordEntity saved = moveRecordRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MOVE_RECORD_REJECTED", ENTITY_MOVE_RECORD, saved.getId(), null);
        return toResponse(saved);
    }

    private MoveRecordResponse create(MoveRecordCreateRequest request, MoveType moveType, ShieldPrincipal principal) {
        enforceSelfOrPrivileged(principal, request.userId());

        unitRepository.findByIdAndDeletedFalse(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.unitId()));
        userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));

        MoveRecordEntity entity = new MoveRecordEntity();
        entity.setTenantId(principal.tenantId());
        entity.setUnitId(request.unitId());
        entity.setUserId(request.userId());
        entity.setMoveType(moveType);
        entity.setEffectiveDate(request.effectiveDate());
        entity.setSecurityDeposit(request.securityDeposit());
        entity.setAgreementUrl(request.agreementUrl());
        entity.setStatus(MoveStatus.PENDING);

        MoveRecordEntity saved = moveRecordRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MOVE_RECORD_CREATED", ENTITY_MOVE_RECORD, saved.getId(), null);
        return toResponse(saved);
    }

    private MoveRecordEntity findEntity(UUID id) {
        return moveRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Move record not found: " + id));
    }

    private void enforceSelfOrPrivileged(ShieldPrincipal principal, UUID targetUserId) {
        if (isPrivileged(principal)) {
            return;
        }
        if (!principal.userId().equals(targetUserId)) {
            throw new UnauthorizedException("You are not allowed to access this move record");
        }
    }

    private boolean isPrivileged(ShieldPrincipal principal) {
        return "ADMIN".equals(principal.role()) || "COMMITTEE".equals(principal.role());
    }

    private MoveRecordResponse toResponse(MoveRecordEntity entity) {
        return new MoveRecordResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUnitId(),
                entity.getUserId(),
                entity.getMoveType(),
                entity.getEffectiveDate(),
                entity.getSecurityDeposit(),
                entity.getAgreementUrl(),
                entity.getStatus(),
                entity.getDecisionNotes(),
                entity.getApprovedBy(),
                entity.getApprovalDate(),
                entity.getCreatedAt());
    }
}
