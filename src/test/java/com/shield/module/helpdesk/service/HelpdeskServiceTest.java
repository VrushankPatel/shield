package com.shield.module.helpdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.helpdesk.dto.HelpdeskTicketCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResponse;
import com.shield.module.helpdesk.entity.HelpdeskTicketEntity;
import com.shield.module.helpdesk.entity.TicketPriority;
import com.shield.module.helpdesk.repository.HelpdeskCategoryRepository;
import com.shield.module.helpdesk.repository.HelpdeskCommentRepository;
import com.shield.module.helpdesk.repository.HelpdeskTicketRepository;
import com.shield.security.model.ShieldPrincipal;
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
    private AuditLogService auditLogService;

    private HelpdeskService helpdeskService;

    @BeforeEach
    void setUp() {
        helpdeskService = new HelpdeskService(
                helpdeskCategoryRepository,
                helpdeskTicketRepository,
                helpdeskCommentRepository,
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
}
