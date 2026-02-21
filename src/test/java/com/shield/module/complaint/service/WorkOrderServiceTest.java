package com.shield.module.complaint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.complaint.dto.WorkOrderCreateRequest;
import com.shield.module.complaint.dto.WorkOrderResponse;
import com.shield.module.complaint.entity.ComplaintEntity;
import com.shield.module.complaint.entity.WorkOrderEntity;
import com.shield.module.complaint.entity.WorkOrderStatus;
import com.shield.module.complaint.repository.ComplaintRepository;
import com.shield.module.complaint.repository.WorkOrderRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private AuditLogService auditLogService;

    private WorkOrderService workOrderService;

    @BeforeEach
    void setUp() {
        workOrderService = new WorkOrderService(workOrderRepository, complaintRepository, auditLogService);
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldSetPendingStatusAndNumber() {
        UUID complaintId = UUID.randomUUID();

        ComplaintEntity complaint = new ComplaintEntity();
        complaint.setId(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));
        when(workOrderRepository.save(any(WorkOrderEntity.class))).thenAnswer(invocation -> {
            WorkOrderEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        WorkOrderResponse response = workOrderService.create(new WorkOrderCreateRequest(
                complaintId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Replace damaged motor",
                new BigDecimal("1500.00"),
                LocalDate.now().plusDays(1)));

        assertEquals(WorkOrderStatus.PENDING, response.status());
        assertNotNull(response.workOrderNumber());
    }

    @Test
    void startShouldTransitionToInProgress() {
        UUID id = UUID.randomUUID();
        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setId(id);
        entity.setTenantId(UUID.randomUUID());
        entity.setStatus(WorkOrderStatus.PENDING);

        when(workOrderRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(workOrderRepository.save(any(WorkOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = workOrderService.start(id);

        assertEquals(WorkOrderStatus.IN_PROGRESS, response.status());
    }

    @Test
    void completeShouldSetCompletionDate() {
        UUID id = UUID.randomUUID();
        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setId(id);
        entity.setTenantId(UUID.randomUUID());
        entity.setStatus(WorkOrderStatus.IN_PROGRESS);

        when(workOrderRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(workOrderRepository.save(any(WorkOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = workOrderService.complete(id);

        assertEquals(WorkOrderStatus.COMPLETED, response.status());
        assertNotNull(response.completionDate());
    }
}
