package com.shield.module.staff.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.staff.dto.StaffLeaveBalanceResponse;
import com.shield.module.staff.dto.StaffLeaveCreateRequest;
import com.shield.module.staff.dto.StaffLeaveResponse;
import com.shield.module.staff.dto.StaffLeaveUpdateRequest;
import com.shield.module.staff.entity.StaffEntity;
import com.shield.module.staff.entity.StaffLeaveEntity;
import com.shield.module.staff.entity.StaffLeaveStatus;
import com.shield.module.staff.repository.StaffLeaveRepository;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StaffLeaveService {

    private final StaffLeaveRepository staffLeaveRepository;
    private final StaffRepository staffRepository;
    private final AuditLogService auditLogService;

    public StaffLeaveService(
            StaffLeaveRepository staffLeaveRepository,
            StaffRepository staffRepository,
            AuditLogService auditLogService) {
        this.staffLeaveRepository = staffLeaveRepository;
        this.staffRepository = staffRepository;
        this.auditLogService = auditLogService;
    }

    public StaffLeaveResponse create(StaffLeaveCreateRequest request, ShieldPrincipal principal) {
        StaffEntity staff = requireStaff(request.staffId());
        validateLeaveDates(request.fromDate(), request.toDate(), request.numberOfDays());

        StaffLeaveEntity entity = new StaffLeaveEntity();
        entity.setTenantId(principal.tenantId());
        entity.setStaffId(staff.getId());
        entity.setLeaveType(request.leaveType());
        entity.setFromDate(request.fromDate());
        entity.setToDate(request.toDate());
        entity.setNumberOfDays(request.numberOfDays());
        entity.setReason(request.reason());
        entity.setStatus(StaffLeaveStatus.PENDING);

        StaffLeaveEntity saved = staffLeaveRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_LEAVE_CREATED", "staff_leave", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffLeaveResponse> list(Pageable pageable) {
        return PagedResponse.from(staffLeaveRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public StaffLeaveResponse getById(UUID id) {
        StaffLeaveEntity entity = staffLeaveRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff leave not found: " + id));
        return toResponse(entity);
    }

    public StaffLeaveResponse update(UUID id, StaffLeaveUpdateRequest request, ShieldPrincipal principal) {
        StaffLeaveEntity entity = staffLeaveRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff leave not found: " + id));

        if (entity.getStatus() != StaffLeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be updated");
        }

        validateLeaveDates(request.fromDate(), request.toDate(), request.numberOfDays());
        entity.setLeaveType(request.leaveType());
        entity.setFromDate(request.fromDate());
        entity.setToDate(request.toDate());
        entity.setNumberOfDays(request.numberOfDays());
        entity.setReason(request.reason());

        StaffLeaveEntity saved = staffLeaveRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_LEAVE_UPDATED", "staff_leave", saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        StaffLeaveEntity entity = staffLeaveRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff leave not found: " + id));

        entity.setDeleted(true);
        staffLeaveRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_LEAVE_DELETED", "staff_leave", entity.getId(), null);
    }

    public StaffLeaveResponse approve(UUID id, ShieldPrincipal principal) {
        StaffLeaveEntity entity = staffLeaveRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff leave not found: " + id));

        entity.setStatus(StaffLeaveStatus.APPROVED);
        entity.setApprovedBy(principal.userId());
        entity.setApprovalDate(LocalDate.now());

        StaffLeaveEntity saved = staffLeaveRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_LEAVE_APPROVED", "staff_leave", saved.getId(), null);
        return toResponse(saved);
    }

    public StaffLeaveResponse reject(UUID id, ShieldPrincipal principal) {
        StaffLeaveEntity entity = staffLeaveRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff leave not found: " + id));

        entity.setStatus(StaffLeaveStatus.REJECTED);
        entity.setApprovedBy(principal.userId());
        entity.setApprovalDate(LocalDate.now());

        StaffLeaveEntity saved = staffLeaveRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_LEAVE_REJECTED", "staff_leave", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffLeaveResponse> listByStaff(UUID staffId, Pageable pageable) {
        return PagedResponse.from(staffLeaveRepository.findAllByStaffIdAndDeletedFalse(staffId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffLeaveResponse> listPending(Pageable pageable) {
        return PagedResponse.from(
                staffLeaveRepository.findAllByStatusAndDeletedFalse(StaffLeaveStatus.PENDING, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public StaffLeaveBalanceResponse getBalance(UUID staffId) {
        requireStaff(staffId);
        List<StaffLeaveEntity> leaves = staffLeaveRepository.findAllByStaffIdAndDeletedFalse(staffId);

        int approvedDays = leaves.stream()
                .filter(leave -> leave.getStatus() == StaffLeaveStatus.APPROVED)
                .mapToInt(StaffLeaveEntity::getNumberOfDays)
                .sum();

        int pendingDays = leaves.stream()
                .filter(leave -> leave.getStatus() == StaffLeaveStatus.PENDING)
                .mapToInt(StaffLeaveEntity::getNumberOfDays)
                .sum();

        int rejectedDays = leaves.stream()
                .filter(leave -> leave.getStatus() == StaffLeaveStatus.REJECTED)
                .mapToInt(StaffLeaveEntity::getNumberOfDays)
                .sum();

        return new StaffLeaveBalanceResponse(staffId, leaves.size(), approvedDays, pendingDays, rejectedDays);
    }

    private StaffEntity requireStaff(UUID staffId) {
        return staffRepository.findByIdAndDeletedFalse(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + staffId));
    }

    private void validateLeaveDates(LocalDate fromDate, LocalDate toDate, int numberOfDays) {
        if (toDate.isBefore(fromDate)) {
            throw new BadRequestException("toDate cannot be before fromDate");
        }
        long span = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (numberOfDays > span) {
            throw new BadRequestException("numberOfDays cannot exceed date span");
        }
    }

    private StaffLeaveResponse toResponse(StaffLeaveEntity entity) {
        return new StaffLeaveResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getStaffId(),
                entity.getLeaveType(),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getNumberOfDays(),
                entity.getReason(),
                entity.getStatus(),
                entity.getApprovedBy(),
                entity.getApprovalDate());
    }
}
