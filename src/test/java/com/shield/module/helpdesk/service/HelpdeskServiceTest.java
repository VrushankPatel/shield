package com.shield.module.helpdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.helpdesk.dto.HelpdeskTicketCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketRateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResponse;
import com.shield.module.helpdesk.entity.HelpdeskTicketEntity;
import com.shield.module.helpdesk.entity.TicketPriority;
import com.shield.module.helpdesk.entity.TicketStatus;
import com.shield.module.helpdesk.repository.HelpdeskCategoryRepository;
import com.shield.module.helpdesk.repository.HelpdeskCommentRepository;
import com.shield.module.helpdesk.repository.HelpdeskTicketAttachmentRepository;
import com.shield.module.helpdesk.repository.HelpdeskTicketRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpdeskServiceTest {

    @Mock
    private HelpdeskCategoryRepository helpdeskCategoryRepository;

    @Mock
    private HelpdeskTicketRepository helpdeskTicketRepository;

    @Mock
    private HelpdeskCommentRepository helpdeskCommentRepository;

    @Mock
    private HelpdeskTicketAttachmentRepository helpdeskTicketAttachmentRepository;

    @Mock
    private AuditLogService auditLogService;

    private HelpdeskService helpdeskService;

    @BeforeEach
    void setUp() {
        helpdeskService = new HelpdeskService(
                helpdeskCategoryRepository,
                helpdeskTicketRepository,
                helpdeskCommentRepository,
                helpdeskTicketAttachmentRepository,
                auditLogService);
    }

    @Test
    void createTicketShouldSetOpenStatusAndTicketNumber() {
        when(helpdeskTicketRepository.save(any(HelpdeskTicketEntity.class))).thenAnswer(invocation -> {
            HelpdeskTicketEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "tenant@shield.dev", "TENANT");
        HelpdeskTicketCreateRequest request = new HelpdeskTicketCreateRequest(
                null,
                UUID.randomUUID(),
                "Water leak",
                "Leak in kitchen",
                TicketPriority.HIGH);

        HelpdeskTicketResponse response = helpdeskService.createTicket(request, principal);

        assertEquals(principal.userId(), response.raisedBy());
        assertEquals(principal.tenantId(), response.tenantId());
        assertEquals("OPEN", response.status().name());
        assertTrue(response.ticketNumber().startsWith("HD-"));
    }

    @Test
    void closeTicketShouldSetClosedStatusAndTimestamp() {
        UUID ticketId = UUID.randomUUID();
        HelpdeskTicketEntity entity = new HelpdeskTicketEntity();
        entity.setId(ticketId);
        entity.setStatus(TicketStatus.RESOLVED);

        when(helpdeskTicketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(entity));
        when(helpdeskTicketRepository.save(any(HelpdeskTicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "committee@shield.dev", "COMMITTEE");
        HelpdeskTicketResponse response = helpdeskService.closeTicket(ticketId, principal);

        assertEquals(TicketStatus.CLOSED, response.status());
        assertNotNull(response.closedAt());
        verify(helpdeskTicketRepository).save(entity);
    }

    @Test
    void rateTicketShouldRejectOpenTicket() {
        UUID ticketId = UUID.randomUUID();
        HelpdeskTicketEntity entity = new HelpdeskTicketEntity();
        entity.setId(ticketId);
        entity.setStatus(TicketStatus.OPEN);

        when(helpdeskTicketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(entity));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "tenant@shield.dev", "TENANT");
        assertThrows(BadRequestException.class, () -> helpdeskService.rateTicket(ticketId, new HelpdeskTicketRateRequest(5), principal));
    }
}
