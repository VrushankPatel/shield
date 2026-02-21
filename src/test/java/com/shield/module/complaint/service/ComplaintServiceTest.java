package com.shield.module.complaint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.complaint.dto.ComplaintAssignRequest;
import com.shield.module.complaint.dto.ComplaintCreateRequest;
import com.shield.module.complaint.dto.ComplaintResponse;
import com.shield.module.complaint.entity.ComplaintEntity;
import com.shield.module.complaint.entity.ComplaintPriority;
import com.shield.module.complaint.entity.ComplaintStatus;
import com.shield.module.complaint.repository.ComplaintCommentRepository;
import com.shield.module.complaint.repository.ComplaintRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
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
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintCommentRepository complaintCommentRepository;

    @Mock
    private AuditLogService auditLogService;

    private ComplaintService complaintService;

    @BeforeEach
    void setUp() {
        complaintService = new ComplaintService(complaintRepository, complaintCommentRepository, auditLogService);
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "test@shield.dev", "ADMIN");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldSetOpenStatusAndComplaintNumber() {
        when(complaintRepository.save(any(ComplaintEntity.class))).thenAnswer(invocation -> {
            ComplaintEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ComplaintResponse response = complaintService.create(new ComplaintCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Water leakage",
                "Leak near pump room",
                ComplaintPriority.HIGH,
                "PLUMBING",
                "Pump room",
                6));

        assertEquals(ComplaintStatus.OPEN, response.status());
        assertNotNull(response.complaintNumber());
        assertEquals("PLUMBING", response.complaintType());
    }

    @Test
    void assignShouldSetAssignedStatus() {
        UUID complaintId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();

        ComplaintEntity entity = new ComplaintEntity();
        entity.setId(complaintId);
        entity.setTenantId(UUID.randomUUID());
        entity.setStatus(ComplaintStatus.OPEN);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(entity));
        when(complaintRepository.save(any(ComplaintEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = complaintService.assign(complaintId, new ComplaintAssignRequest(assignee));

        assertEquals(ComplaintStatus.ASSIGNED, response.status());
        assertEquals(assignee, response.assignedTo());
        assertNotNull(response.assignedAt());
    }

    @Test
    void closeShouldSetClosedStatus() {
        UUID complaintId = UUID.randomUUID();

        ComplaintEntity entity = new ComplaintEntity();
        entity.setId(complaintId);
        entity.setTenantId(UUID.randomUUID());
        entity.setStatus(ComplaintStatus.RESOLVED);
        entity.setResolvedAt(Instant.now());

        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(entity));
        when(complaintRepository.save(any(ComplaintEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = complaintService.close(complaintId);

        assertEquals(ComplaintStatus.CLOSED, response.status());
        assertNotNull(response.closedAt());
    }

    @Test
    void resolveShouldThrowWhenComplaintMissing() {
        UUID complaintId = UUID.randomUUID();
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> complaintService.resolve(complaintId, null));
    }
}
