package com.shield.module.helpdesk.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.helpdesk.dto.HelpdeskCategoryCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCategoryResponse;
import com.shield.module.helpdesk.dto.HelpdeskCategoryUpdateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCommentCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCommentResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketAssignRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResolveRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketUpdateRequest;
import com.shield.module.helpdesk.entity.HelpdeskCategoryEntity;
import com.shield.module.helpdesk.entity.HelpdeskCommentEntity;
import com.shield.module.helpdesk.entity.HelpdeskTicketEntity;
import com.shield.module.helpdesk.entity.TicketStatus;
import com.shield.module.helpdesk.repository.HelpdeskCategoryRepository;
import com.shield.module.helpdesk.repository.HelpdeskCommentRepository;
import com.shield.module.helpdesk.repository.HelpdeskTicketRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HelpdeskService {

    private final HelpdeskCategoryRepository helpdeskCategoryRepository;
    private final HelpdeskTicketRepository helpdeskTicketRepository;
    private final HelpdeskCommentRepository helpdeskCommentRepository;
    private final AuditLogService auditLogService;

    public HelpdeskService(
            HelpdeskCategoryRepository helpdeskCategoryRepository,
            HelpdeskTicketRepository helpdeskTicketRepository,
            HelpdeskCommentRepository helpdeskCommentRepository,
            AuditLogService auditLogService) {
        this.helpdeskCategoryRepository = helpdeskCategoryRepository;
        this.helpdeskTicketRepository = helpdeskTicketRepository;
        this.helpdeskCommentRepository = helpdeskCommentRepository;
        this.auditLogService = auditLogService;
    }

    public HelpdeskCategoryResponse createCategory(HelpdeskCategoryCreateRequest request, ShieldPrincipal principal) {
        HelpdeskCategoryEntity entity = new HelpdeskCategoryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSlaHours(request.slaHours());

        HelpdeskCategoryEntity saved = helpdeskCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_CATEGORY_CREATED", "helpdesk_category", saved.getId(), null);
        return toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskCategoryResponse> listCategories(Pageable pageable) {
        return PagedResponse.from(helpdeskCategoryRepository.findAllByDeletedFalse(pageable).map(this::toCategoryResponse));
    }

    @Transactional(readOnly = true)
    public HelpdeskCategoryResponse getCategory(UUID id) {
        HelpdeskCategoryEntity entity = helpdeskCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk category not found: " + id));
        return toCategoryResponse(entity);
    }

    public HelpdeskCategoryResponse updateCategory(UUID id, HelpdeskCategoryUpdateRequest request, ShieldPrincipal principal) {
        HelpdeskCategoryEntity entity = helpdeskCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk category not found: " + id));

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSlaHours(request.slaHours());

        HelpdeskCategoryEntity saved = helpdeskCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_CATEGORY_UPDATED", "helpdesk_category", saved.getId(), null);
        return toCategoryResponse(saved);
    }

    public void deleteCategory(UUID id, ShieldPrincipal principal) {
        HelpdeskCategoryEntity entity = helpdeskCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk category not found: " + id));

        entity.setDeleted(true);
        helpdeskCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_CATEGORY_DELETED", "helpdesk_category", entity.getId(), null);
    }

    public HelpdeskTicketResponse createTicket(HelpdeskTicketCreateRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = new HelpdeskTicketEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTicketNumber("HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setCategoryId(request.categoryId());
        entity.setRaisedBy(principal.userId());
        entity.setUnitId(request.unitId());
        entity.setSubject(request.subject());
        entity.setDescription(request.description());
        entity.setPriority(request.priority());
        entity.setStatus(TicketStatus.OPEN);

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_CREATED", "helpdesk_ticket", saved.getId(), null);
        return toTicketResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskTicketResponse> listTickets(Pageable pageable) {
        return PagedResponse.from(helpdeskTicketRepository.findAllByDeletedFalse(pageable).map(this::toTicketResponse));
    }

    @Transactional(readOnly = true)
    public HelpdeskTicketResponse getTicket(UUID id) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk ticket not found: " + id));
        return toTicketResponse(entity);
    }

    public HelpdeskTicketResponse updateTicket(UUID id, HelpdeskTicketUpdateRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk ticket not found: " + id));

        entity.setCategoryId(request.categoryId());
        entity.setUnitId(request.unitId());
        entity.setSubject(request.subject());
        entity.setDescription(request.description());
        entity.setPriority(request.priority());

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_UPDATED", "helpdesk_ticket", saved.getId(), null);
        return toTicketResponse(saved);
    }

    public void deleteTicket(UUID id, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk ticket not found: " + id));

        entity.setDeleted(true);
        helpdeskTicketRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_DELETED", "helpdesk_ticket", entity.getId(), null);
    }

    public HelpdeskTicketResponse assignTicket(UUID id, HelpdeskTicketAssignRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk ticket not found: " + id));

        entity.setAssignedTo(request.assignedTo());
        entity.setAssignedAt(Instant.now());
        entity.setStatus(TicketStatus.IN_PROGRESS);

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_ASSIGNED", "helpdesk_ticket", saved.getId(), null);
        return toTicketResponse(saved);
    }

    public HelpdeskTicketResponse resolveTicket(UUID id, HelpdeskTicketResolveRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk ticket not found: " + id));

        entity.setStatus(TicketStatus.RESOLVED);
        entity.setResolvedAt(Instant.now());
        entity.setResolutionNotes(request.resolutionNotes());

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_RESOLVED", "helpdesk_ticket", saved.getId(), null);
        return toTicketResponse(saved);
    }

    public HelpdeskCommentResponse addComment(UUID ticketId, HelpdeskCommentCreateRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity ticket = helpdeskTicketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk ticket not found: " + ticketId));

        HelpdeskCommentEntity comment = new HelpdeskCommentEntity();
        comment.setTenantId(ticket.getTenantId());
        comment.setTicketId(ticketId);
        comment.setUserId(principal.userId());
        comment.setComment(request.comment());
        comment.setInternalNote(request.internalNote());

        HelpdeskCommentEntity saved = helpdeskCommentRepository.save(comment);
        auditLogService.record(principal.tenantId(), principal.userId(), "HELPDESK_COMMENT_ADDED", "helpdesk_comment", saved.getId(), null);
        return toCommentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskCommentResponse> listComments(UUID ticketId, Pageable pageable) {
        return PagedResponse.from(helpdeskCommentRepository.findAllByTicketIdAndDeletedFalse(ticketId, pageable).map(this::toCommentResponse));
    }

    private HelpdeskCategoryResponse toCategoryResponse(HelpdeskCategoryEntity entity) {
        return new HelpdeskCategoryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSlaHours());
    }

    private HelpdeskTicketResponse toTicketResponse(HelpdeskTicketEntity entity) {
        return new HelpdeskTicketResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTicketNumber(),
                entity.getCategoryId(),
                entity.getRaisedBy(),
                entity.getUnitId(),
                entity.getSubject(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getAssignedTo(),
                entity.getAssignedAt(),
                entity.getResolvedAt(),
                entity.getResolutionNotes());
    }

    private HelpdeskCommentResponse toCommentResponse(HelpdeskCommentEntity entity) {
        return new HelpdeskCommentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTicketId(),
                entity.getUserId(),
                entity.getComment(),
                entity.isInternalNote(),
                entity.getCreatedAt());
    }
}
