package com.shield.module.staff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.staff.dto.StaffLeaveBalanceResponse;
import com.shield.module.staff.dto.StaffLeaveCreateRequest;
import com.shield.module.staff.dto.StaffLeaveResponse;
import com.shield.module.staff.dto.StaffLeaveUpdateRequest;
import com.shield.module.staff.entity.StaffEntity;
import com.shield.module.staff.entity.StaffLeaveEntity;
import com.shield.module.staff.entity.StaffLeaveStatus;
import com.shield.module.staff.entity.StaffLeaveType;
import com.shield.module.staff.repository.StaffLeaveRepository;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffLeaveServiceTest {

    @Mock
    private StaffLeaveRepository staffLeaveRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AuditLogService auditLogService;

    private StaffLeaveService staffLeaveService;

    @BeforeEach
    void setUp() {
        staffLeaveService = new StaffLeaveService(staffLeaveRepository, staffRepository, auditLogService);
    }

    @Test
    void createShouldStorePendingLeave() {
        UUID tenantId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffLeaveRepository.save(any(StaffLeaveEntity.class))).thenAnswer(invocation -> {
            StaffLeaveEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");
        StaffLeaveResponse response = staffLeaveService.create(
                new StaffLeaveCreateRequest(
                        staffId,
                        StaffLeaveType.CASUAL,
                        LocalDate.of(2026, 2, 10),
                        LocalDate.of(2026, 2, 12),
                        3,
                        "Family function"),
                principal);

        assertEquals(staffId, response.staffId());
        assertEquals(StaffLeaveStatus.PENDING, response.status());
        assertEquals(3, response.numberOfDays());
    }

    @Test
    void createShouldRejectInvalidDateSpan() {
        UUID staffId = UUID.randomUUID();
        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        StaffLeaveCreateRequest request = new StaffLeaveCreateRequest(
                staffId,
                StaffLeaveType.SICK,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                4,
                "Medical");

        assertThrows(BadRequestException.class, () -> staffLeaveService.create(request, principal));
    }

    @Test
    void updateShouldRejectWhenNotPending() {
        UUID leaveId = UUID.randomUUID();
        StaffLeaveEntity leave = new StaffLeaveEntity();
        leave.setId(leaveId);
        leave.setStatus(StaffLeaveStatus.APPROVED);

        when(staffLeaveRepository.findByIdAndDeletedFalse(leaveId)).thenReturn(Optional.of(leave));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        StaffLeaveUpdateRequest request = new StaffLeaveUpdateRequest(
                StaffLeaveType.CASUAL,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 1),
                1,
                "Changed");

        assertThrows(BadRequestException.class, () -> staffLeaveService.update(leaveId, request, principal));
    }

    @Test
    void balanceShouldAggregateByStatus() {
        UUID staffId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));

        StaffLeaveEntity approved = new StaffLeaveEntity();
        approved.setStatus(StaffLeaveStatus.APPROVED);
        approved.setNumberOfDays(2);

        StaffLeaveEntity pending = new StaffLeaveEntity();
        pending.setStatus(StaffLeaveStatus.PENDING);
        pending.setNumberOfDays(1);

        StaffLeaveEntity rejected = new StaffLeaveEntity();
        rejected.setStatus(StaffLeaveStatus.REJECTED);
        rejected.setNumberOfDays(3);

        when(staffLeaveRepository.findAllByStaffIdAndDeletedFalse(staffId)).thenReturn(List.of(approved, pending, rejected));

        StaffLeaveBalanceResponse response = staffLeaveService.getBalance(staffId);

        assertEquals(3, response.totalRequests());
        assertEquals(2, response.approvedDays());
        assertEquals(1, response.pendingDays());
        assertEquals(3, response.rejectedDays());
    }
}
