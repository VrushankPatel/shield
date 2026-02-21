package com.shield.module.staff.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.staff.dto.StaffAttendanceCheckInRequest;
import com.shield.module.staff.dto.StaffAttendanceCheckOutRequest;
import com.shield.module.staff.dto.StaffAttendanceResponse;
import com.shield.module.staff.dto.StaffAttendanceSummaryResponse;
import com.shield.module.staff.dto.StaffAttendanceUpdateRequest;
import com.shield.module.staff.dto.StaffCreateRequest;
import com.shield.module.staff.dto.StaffResponse;
import com.shield.module.staff.dto.StaffStatusUpdateRequest;
import com.shield.module.staff.dto.StaffUpdateRequest;
import com.shield.common.exception.BadRequestException;
import com.shield.module.staff.entity.StaffAttendanceEntity;
import com.shield.module.staff.entity.StaffAttendanceStatus;
import com.shield.module.staff.entity.StaffEntity;
import com.shield.module.staff.repository.StaffAttendanceRepository;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StaffService {

    private static final String ENTITY_STAFF_ATTENDANCE = "staff_attendance";

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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_CREATED", "staff", saved.getId(), null);
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
    public PagedResponse<StaffResponse> listByDesignation(String designation, Pageable pageable) {
        return PagedResponse.from(
                staffRepository.findAllByDesignationIgnoreCaseAndDeletedFalse(designation, pageable).map(this::toResponse));
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_UPDATED", "staff", saved.getId(), null);
        return toResponse(saved);
    }

    public StaffResponse updateStatus(UUID id, StaffStatusUpdateRequest request, ShieldPrincipal principal) {
        StaffEntity entity = staffRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));

        entity.setActive(request.active());
        StaffEntity saved = staffRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_STATUS_UPDATED", "staff", saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        StaffEntity entity = staffRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));

        entity.setDeleted(true);
        staffRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_DELETED", "staff", entity.getId(), null);
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_CHECK_IN", ENTITY_STAFF_ATTENDANCE, saved.getId(), null);
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_CHECK_OUT", ENTITY_STAFF_ATTENDANCE, saved.getId(), null);
        return toAttendanceResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffAttendanceResponse> listAttendanceByStaff(UUID staffId, Pageable pageable) {
        return PagedResponse.from(staffAttendanceRepository.findAllByStaffIdAndDeletedFalse(staffId, pageable)
                .map(this::toAttendanceResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffAttendanceResponse> listAttendance(Pageable pageable) {
        return PagedResponse.from(staffAttendanceRepository.findAllByDeletedFalse(pageable).map(this::toAttendanceResponse));
    }

    @Transactional(readOnly = true)
    public StaffAttendanceResponse getAttendanceById(UUID id) {
        StaffAttendanceEntity entity = staffAttendanceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff attendance not found: " + id));
        return toAttendanceResponse(entity);
    }

    public StaffAttendanceResponse updateAttendance(UUID id, StaffAttendanceUpdateRequest request, ShieldPrincipal principal) {
        StaffAttendanceEntity entity = staffAttendanceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff attendance not found: " + id));

        entity.setStatus(request.status());
        entity.setCheckInTime(request.checkInTime());
        entity.setCheckOutTime(request.checkOutTime());
        entity.setMarkedBy(principal.userId());

        if (entity.getCheckOutTime() != null && entity.getCheckInTime() == null) {
            throw new BadRequestException("checkOutTime cannot be set when checkInTime is missing");
        }
        if (entity.getCheckInTime() != null
                && entity.getCheckOutTime() != null
                && entity.getCheckOutTime().isBefore(entity.getCheckInTime())) {
            throw new BadRequestException("checkOutTime cannot be before checkInTime");
        }

        StaffAttendanceEntity saved = staffAttendanceRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "STAFF_ATTENDANCE_UPDATED", ENTITY_STAFF_ATTENDANCE, saved.getId(), null);
        return toAttendanceResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffAttendanceResponse> listAttendanceByDate(LocalDate date, Pageable pageable) {
        return PagedResponse.from(staffAttendanceRepository.findAllByAttendanceDateAndDeletedFalse(date, pageable)
                .map(this::toAttendanceResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<StaffAttendanceResponse> listAttendanceByDateRange(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        validateDateRange(fromDate, toDate);
        return PagedResponse.from(staffAttendanceRepository.findAllByAttendanceDateBetweenAndDeletedFalse(fromDate, toDate, pageable)
                .map(this::toAttendanceResponse));
    }

    @Transactional(readOnly = true)
    public StaffAttendanceSummaryResponse summarizeAttendance(LocalDate fromDate, LocalDate toDate) {
        LocalDate effectiveFrom = fromDate;
        LocalDate effectiveTo = toDate;
        if (effectiveFrom == null || effectiveTo == null) {
            YearMonth currentMonth = YearMonth.now();
            effectiveFrom = currentMonth.atDay(1);
            effectiveTo = currentMonth.atEndOfMonth();
        }
        validateDateRange(effectiveFrom, effectiveTo);

        List<StaffAttendanceEntity> rows = staffAttendanceRepository
                .findAllByAttendanceDateBetweenAndDeletedFalse(effectiveFrom, effectiveTo);

        long present = rows.stream().filter(r -> r.getStatus() == StaffAttendanceStatus.PRESENT).count();
        long absent = rows.stream().filter(r -> r.getStatus() == StaffAttendanceStatus.ABSENT).count();
        long halfDay = rows.stream().filter(r -> r.getStatus() == StaffAttendanceStatus.HALF_DAY).count();
        long leave = rows.stream().filter(r -> r.getStatus() == StaffAttendanceStatus.LEAVE).count();
        long openCheckOut = rows.stream()
                .filter(r -> r.getStatus() == StaffAttendanceStatus.PRESENT)
                .filter(r -> r.getCheckInTime() != null && r.getCheckOutTime() == null)
                .count();

        return new StaffAttendanceSummaryResponse(
                effectiveFrom,
                effectiveTo,
                rows.size(),
                present,
                absent,
                halfDay,
                leave,
                openCheckOut);
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        List<StaffEntity> rows = staffRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("id,tenantId,employeeId,firstName,lastName,phone,email,designation,dateOfJoining,dateOfLeaving,employmentType,basicSalary,active,createdAt\n");
        for (StaffEntity row : rows) {
            csv.append(csv(row.getId())).append(',')
                    .append(csv(row.getTenantId())).append(',')
                    .append(csv(row.getEmployeeId())).append(',')
                    .append(csv(row.getFirstName())).append(',')
                    .append(csv(row.getLastName())).append(',')
                    .append(csv(row.getPhone())).append(',')
                    .append(csv(row.getEmail())).append(',')
                    .append(csv(row.getDesignation())).append(',')
                    .append(csv(row.getDateOfJoining())).append(',')
                    .append(csv(row.getDateOfLeaving())).append(',')
                    .append(csv(row.getEmploymentType())).append(',')
                    .append(csv(row.getBasicSalary())).append(',')
                    .append(csv(row.isActive())).append(',')
                    .append(csv(row.getCreatedAt()))
                    .append('\n');
        }
        return csv.toString();
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BadRequestException("Both from and to dates are required");
        }
        if (toDate.isBefore(fromDate)) {
            throw new BadRequestException("to date cannot be before from date");
        }
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

    private String csv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = value.toString().replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
