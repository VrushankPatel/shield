package com.shield.module.staff.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.staff.dto.StaffAttendanceCheckInRequest;
import com.shield.module.staff.dto.StaffAttendanceCheckOutRequest;
import com.shield.module.staff.dto.StaffAttendanceResponse;
import com.shield.module.staff.dto.StaffCreateRequest;
import com.shield.module.staff.dto.StaffResponse;
import com.shield.module.staff.dto.StaffStatusUpdateRequest;
import com.shield.module.staff.dto.StaffUpdateRequest;
import com.shield.module.staff.entity.StaffAttendanceEntity;
import com.shield.module.staff.entity.StaffAttendanceStatus;
import com.shield.module.staff.entity.StaffEntity;
import com.shield.module.staff.repository.StaffAttendanceRepository;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;
    private final AuditLogService auditLogService;

    public StaffService(
            StaffRepository staffRepository,
            StaffAttendanceRepository staffAttendanceRepository,
            AuditLogService auditLogService) {
        this.staffRepository = staffRepository;
        this.staffAttendanceRepository = staffAttendanceRepository;
        this.auditLogService = auditLogService;
    }

    public StaffResponse create(StaffCreateRequest request, ShieldPrincipal principal) {
        StaffEntity entity = new StaffEntity();
        entity.setTenantId(principal.tenantId());
        entity.setEmployeeId(request.employeeId());
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setDesignation(request.designation());
        entity.setDateOfJoining(request.dateOfJoining());
        entity.setDateOfLeaving(request.dateOfLeaving());
        entity.setEmploymentType(request.employmentType());
        entity.setBasicSalary(request.basicSalary());
        entity.setActive(request.active());

        StaffEntity saved = staffRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "STAFF_CREATED", "staff", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffResponse> list(Pageable pageable) {
        return PagedResponse.from(staffRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffResponse> listActive(Pageable pageable) {
        return PagedResponse.from(staffRepository.findAllByActiveTrueAndDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public StaffResponse getById(UUID id) {
        StaffEntity entity = staffRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        return toResponse(entity);
    }

    public StaffResponse update(UUID id, StaffUpdateRequest request, ShieldPrincipal principal) {
        StaffEntity entity = staffRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));

        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setDesignation(request.designation());
        entity.setDateOfJoining(request.dateOfJoining());
        entity.setDateOfLeaving(request.dateOfLeaving());
        entity.setEmploymentType(request.employmentType());
        entity.setBasicSalary(request.basicSalary());
        entity.setActive(request.active());

        StaffEntity saved = staffRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "STAFF_UPDATED", "staff", saved.getId(), null);
        return toResponse(saved);
    }

    public StaffResponse updateStatus(UUID id, StaffStatusUpdateRequest request, ShieldPrincipal principal) {
        StaffEntity entity = staffRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));

        entity.setActive(request.active());
        StaffEntity saved = staffRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "STAFF_STATUS_UPDATED", "staff", saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        StaffEntity entity = staffRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));

        entity.setDeleted(true);
        staffRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "STAFF_DELETED", "staff", entity.getId(), null);
    }

    public StaffAttendanceResponse checkIn(StaffAttendanceCheckInRequest request, ShieldPrincipal principal) {
        StaffEntity staff = staffRepository.findByIdAndDeletedFalse(request.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + request.staffId()));

        LocalDate date = request.attendanceDate() != null ? request.attendanceDate() : LocalDate.now();
        StaffAttendanceEntity entity = staffAttendanceRepository.findByStaffIdAndAttendanceDateAndDeletedFalse(staff.getId(), date)
                .orElseGet(() -> {
                    StaffAttendanceEntity attendance = new StaffAttendanceEntity();
                    attendance.setTenantId(principal.tenantId());
                    attendance.setStaffId(staff.getId());
                    attendance.setAttendanceDate(date);
                    return attendance;
                });

        if (entity.getCheckInTime() == null) {
            entity.setCheckInTime(Instant.now());
        }
        entity.setStatus(StaffAttendanceStatus.PRESENT);
        entity.setMarkedBy(principal.userId());

        StaffAttendanceEntity saved = staffAttendanceRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "STAFF_CHECK_IN", "staff_attendance", saved.getId(), null);
        return toAttendanceResponse(saved);
    }

    public StaffAttendanceResponse checkOut(StaffAttendanceCheckOutRequest request, ShieldPrincipal principal) {
        StaffEntity staff = staffRepository.findByIdAndDeletedFalse(request.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + request.staffId()));

        LocalDate date = request.attendanceDate() != null ? request.attendanceDate() : LocalDate.now();
        StaffAttendanceEntity entity = staffAttendanceRepository.findByStaffIdAndAttendanceDateAndDeletedFalse(staff.getId(), date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attendance not found for staff " + request.staffId() + " on " + date));

        if (entity.getCheckInTime() == null) {
            entity.setCheckInTime(Instant.now());
        }
        entity.setCheckOutTime(Instant.now());
        entity.setStatus(StaffAttendanceStatus.PRESENT);
        entity.setMarkedBy(principal.userId());

        StaffAttendanceEntity saved = staffAttendanceRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "STAFF_CHECK_OUT", "staff_attendance", saved.getId(), null);
        return toAttendanceResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffAttendanceResponse> listAttendanceByStaff(UUID staffId, Pageable pageable) {
        return PagedResponse.from(staffAttendanceRepository.findAllByStaffIdAndDeletedFalse(staffId, pageable)
                .map(this::toAttendanceResponse));
    }

    private StaffResponse toResponse(StaffEntity entity) {
        return new StaffResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getEmployeeId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getDesignation(),
                entity.getDateOfJoining(),
                entity.getDateOfLeaving(),
                entity.getEmploymentType(),
                entity.getBasicSalary(),
                entity.isActive());
    }

    private StaffAttendanceResponse toAttendanceResponse(StaffAttendanceEntity entity) {
        return new StaffAttendanceResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getStaffId(),
                entity.getAttendanceDate(),
                entity.getCheckInTime(),
                entity.getCheckOutTime(),
                entity.getStatus(),
                entity.getMarkedBy());
    }
}
