package com.shield.module.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.module.payroll.dto.SalaryStructureCreateRequest;
import com.shield.module.payroll.dto.SalaryStructureResponse;
import com.shield.module.payroll.entity.PayrollComponentEntity;
import com.shield.module.payroll.entity.PayrollComponentType;
import com.shield.module.payroll.entity.StaffSalaryStructureEntity;
import com.shield.module.payroll.repository.PayrollComponentRepository;
import com.shield.module.payroll.repository.StaffSalaryStructureRepository;
import com.shield.module.staff.entity.StaffEntity;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SalaryStructureServiceTest {

    @Mock
    private StaffSalaryStructureRepository staffSalaryStructureRepository;

    @Mock
    private PayrollComponentRepository payrollComponentRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AuditLogService auditLogService;

    private SalaryStructureService salaryStructureService;

    @BeforeEach
    void setUp() {
        salaryStructureService = new SalaryStructureService(
                staffSalaryStructureRepository,
                payrollComponentRepository,
                staffRepository,
                auditLogService);
    }

    @Test
    void createShouldPersistSalaryStructure() {
        UUID staffId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        PayrollComponentEntity component = new PayrollComponentEntity();
        component.setId(componentId);
        component.setComponentName("Basic");
        component.setComponentType(PayrollComponentType.EARNING);
        component.setTaxable(true);

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(payrollComponentRepository.findByIdAndDeletedFalse(componentId)).thenReturn(Optional.of(component));
        when(staffSalaryStructureRepository.existsByStaffIdAndPayrollComponentIdAndEffectiveFromAndDeletedFalse(
                staffId,
                componentId,
                LocalDate.of(2026, 1, 1))).thenReturn(false);
        when(staffSalaryStructureRepository.save(any(StaffSalaryStructureEntity.class))).thenAnswer(invocation -> {
            StaffSalaryStructureEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        SalaryStructureResponse response = salaryStructureService.create(
                staffId,
                new SalaryStructureCreateRequest(componentId, BigDecimal.valueOf(30000), true, LocalDate.of(2026, 1, 1)),
                principal);

        assertEquals(staffId, response.staffId());
        assertEquals(componentId, response.payrollComponentId());
        assertEquals("Basic", response.payrollComponentName());
    }

    @Test
    void createShouldRejectDuplicateEffectiveDate() {
        UUID staffId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        PayrollComponentEntity component = new PayrollComponentEntity();
        component.setId(componentId);

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(payrollComponentRepository.findByIdAndDeletedFalse(componentId)).thenReturn(Optional.of(component));
        when(staffSalaryStructureRepository.existsByStaffIdAndPayrollComponentIdAndEffectiveFromAndDeletedFalse(
                staffId,
                componentId,
                LocalDate.of(2026, 1, 1))).thenReturn(true);

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");

        assertThrows(BadRequestException.class, () -> salaryStructureService.create(
                staffId,
                new SalaryStructureCreateRequest(componentId, BigDecimal.valueOf(30000), true, LocalDate.of(2026, 1, 1)),
                principal));
    }

    @Test
    void listByStaffShouldResolveComponentMetadata() {
        UUID staffId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));

        StaffSalaryStructureEntity row = new StaffSalaryStructureEntity();
        row.setId(UUID.randomUUID());
        row.setTenantId(UUID.randomUUID());
        row.setStaffId(staffId);
        row.setPayrollComponentId(componentId);
        row.setAmount(BigDecimal.valueOf(5000));
        row.setActive(true);
        row.setEffectiveFrom(LocalDate.of(2026, 2, 1));

        PayrollComponentEntity component = new PayrollComponentEntity();
        component.setId(componentId);
        component.setComponentName("HRA");
        component.setComponentType(PayrollComponentType.EARNING);

        when(staffSalaryStructureRepository.findAllByStaffIdAndDeletedFalse(staffId, Pageable.ofSize(5)))
                .thenReturn(new PageImpl<>(List.of(row)));
        when(payrollComponentRepository.findAllByIdInAndDeletedFalse(any())).thenReturn(List.of(component));

        PagedResponse<SalaryStructureResponse> response = salaryStructureService.listByStaff(staffId, Pageable.ofSize(5));

        assertEquals(1, response.content().size());
        assertEquals("HRA", response.content().get(0).payrollComponentName());
    }
}
