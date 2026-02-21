package com.shield.module.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.payroll.dto.PayrollComponentCreateRequest;
import com.shield.module.payroll.dto.PayrollComponentResponse;
import com.shield.module.payroll.dto.PayrollComponentUpdateRequest;
import com.shield.module.payroll.entity.PayrollComponentEntity;
import com.shield.module.payroll.entity.PayrollComponentType;
import com.shield.module.payroll.repository.PayrollComponentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayrollComponentServiceTest {

    @Mock
    private PayrollComponentRepository payrollComponentRepository;

    @Mock
    private AuditLogService auditLogService;

    private PayrollComponentService payrollComponentService;

    @BeforeEach
    void setUp() {
        payrollComponentService = new PayrollComponentService(payrollComponentRepository, auditLogService);
    }

    @Test
    void createShouldPersistComponent() {
        UUID tenantId = UUID.randomUUID();
        when(payrollComponentRepository.existsByComponentNameIgnoreCaseAndDeletedFalse("Basic"))
                .thenReturn(false);
        when(payrollComponentRepository.save(any(PayrollComponentEntity.class))).thenAnswer(invocation -> {
            PayrollComponentEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");
        PayrollComponentResponse response = payrollComponentService.create(
                new PayrollComponentCreateRequest("Basic", PayrollComponentType.EARNING, true),
                principal);

        assertEquals("Basic", response.componentName());
        assertEquals(PayrollComponentType.EARNING, response.componentType());
        assertEquals(tenantId, response.tenantId());
    }

    @Test
    void createShouldRejectDuplicateName() {
        when(payrollComponentRepository.existsByComponentNameIgnoreCaseAndDeletedFalse("Basic"))
                .thenReturn(true);

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        PayrollComponentCreateRequest request = new PayrollComponentCreateRequest("Basic", PayrollComponentType.EARNING, true);

        assertThrows(BadRequestException.class, () -> payrollComponentService.create(request, principal));
    }

    @Test
    void updateShouldRejectDuplicateName() {
        UUID componentId = UUID.randomUUID();
        PayrollComponentEntity entity = new PayrollComponentEntity();
        entity.setId(componentId);

        when(payrollComponentRepository.findByIdAndDeletedFalse(componentId)).thenReturn(Optional.of(entity));
        when(payrollComponentRepository.existsByComponentNameIgnoreCaseAndDeletedFalseAndIdNot("HRA", componentId))
                .thenReturn(true);

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        PayrollComponentUpdateRequest request = new PayrollComponentUpdateRequest("HRA", PayrollComponentType.EARNING, true);

        assertThrows(BadRequestException.class, () -> payrollComponentService.update(componentId, request, principal));
    }
}
