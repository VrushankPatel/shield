package com.shield.module.helpdesk.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.helpdesk.dto.HelpdeskCategoryCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCategoryResponse;
import com.shield.module.helpdesk.dto.HelpdeskCategoryUpdateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCommentCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCommentResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketAssignRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketAttachmentCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketAttachmentResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketRateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResolveRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketStatsResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketUpdateRequest;
import com.shield.module.helpdesk.entity.HelpdeskCategoryEntity;
import com.shield.module.helpdesk.entity.HelpdeskCommentEntity;
import com.shield.module.helpdesk.entity.HelpdeskTicketAttachmentEntity;
import com.shield.module.helpdesk.entity.HelpdeskTicketEntity;
import com.shield.module.helpdesk.entity.TicketStatus;
import com.shield.module.helpdesk.repository.HelpdeskCategoryRepository;
import com.shield.module.helpdesk.repository.HelpdeskCommentRepository;
import com.shield.module.helpdesk.repository.HelpdeskTicketAttachmentRepository;
import com.shield.module.helpdesk.repository.HelpdeskTicketRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HelpdeskService {

    private static final String ENTITY_HELPDESK_CATEGORY = "helpdesk_category";
    private static final String HELPDESK_CATEGORY_NOT_FOUND_PREFIX = "Helpdesk category not found: ";
    private static final String ENTITY_HELPDESK_TICKET = "helpdesk_ticket";
    private static final String HELPDESK_TICKET_NOT_FOUND_PREFIX = "Helpdesk ticket not found: ";

    private final HelpdeskCategoryRepository helpdeskCategoryRepository;
    private final HelpdeskTicketRepository helpdeskTicketRepository;
    private final HelpdeskCommentRepository helpdeskCommentRepository;
    private final HelpdeskTicketAttachmentRepository helpdeskTicketAttachmentRepository;
    private final AuditLogService auditLogService;

    public HelpdeskService(
            HelpdeskCategoryRepository helpdeskCategoryRepository,
            HelpdeskTicketRepository helpdeskTicketRepository,
            HelpdeskCommentRepository helpdeskCommentRepository,
            HelpdeskTicketAttachmentRepository helpdeskTicketAttachmentRepository,
            AuditLogService auditLogService) {
        this.helpdeskCategoryRepository = helpdeskCategoryRepository;
        this.helpdeskTicketRepository = helpdeskTicketRepository;
        this.helpdeskCommentRepository = helpdeskCommentRepository;
        this.helpdeskTicketAttachmentRepository = helpdeskTicketAttachmentRepository;
        this.auditLogService = auditLogService;
    }

    public HelpdeskCategoryResponse createCategory(HelpdeskCategoryCreateRequest request, ShieldPrincipal principal) {
        HelpdeskCategoryEntity entity = new HelpdeskCategoryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSlaHours(request.slaHours());

        HelpdeskCategoryEntity saved = helpdeskCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_CATEGORY_CREATED", ENTITY_HELPDESK_CATEGORY, saved.getId(), null);
        return toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskCategoryResponse> listCategories(Pageable pageable) {
        return PagedResponse.from(helpdeskCategoryRepository.findAllByDeletedFalse(pageable).map(this::toCategoryResponse));
    }

    @Transactional(readOnly = true)
    public HelpdeskCategoryResponse getCategory(UUID id) {
        HelpdeskCategoryEntity entity = helpdeskCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_CATEGORY_NOT_FOUND_PREFIX + id));
        return toCategoryResponse(entity);
    }

    public HelpdeskCategoryResponse updateCategory(UUID id, HelpdeskCategoryUpdateRequest request, ShieldPrincipal principal) {
        HelpdeskCategoryEntity entity = helpdeskCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_CATEGORY_NOT_FOUND_PREFIX + id));

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSlaHours(request.slaHours());

        HelpdeskCategoryEntity saved = helpdeskCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_CATEGORY_UPDATED", ENTITY_HELPDESK_CATEGORY, saved.getId(), null);
        return toCategoryResponse(saved);
    }

    public void deleteCategory(UUID id, ShieldPrincipal principal) {
        HelpdeskCategoryEntity entity = helpdeskCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_CATEGORY_NOT_FOUND_PREFIX + id));

        entity.setDeleted(true);
        helpdeskCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_CATEGORY_DELETED", ENTITY_HELPDESK_CATEGORY, entity.getId(), null);
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
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_CREATED", ENTITY_HELPDESK_TICKET, saved.getId(), null);
        return toTicketResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskTicketResponse> listTickets(Pageable pageable) {
        return PagedResponse.from(helpdeskTicketRepository.findAllByDeletedFalse(pageable).map(this::toTicketResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskTicketResponse> listMyTickets(ShieldPrincipal principal, Pageable pageable) {
        return PagedResponse.from(helpdeskTicketRepository.findAllByRaisedByAndDeletedFalse(principal.userId(), pageable)
                .map(this::toTicketResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskTicketResponse> listAssignedToMe(ShieldPrincipal principal, Pageable pageable) {
        return PagedResponse.from(helpdeskTicketRepository.findAllByAssignedToAndDeletedFalse(principal.userId(), pageable)
                .map(this::toTicketResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskTicketResponse> listByStatus(TicketStatus status, Pageable pageable) {
        return PagedResponse.from(helpdeskTicketRepository.findAllByStatusAndDeletedFalse(status, pageable).map(this::toTicketResponse));
    }

    @Transactional(readOnly = true)
    public HelpdeskTicketResponse getTicket(UUID id) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));
        return toTicketResponse(entity);
    }

    public HelpdeskTicketResponse updateTicket(UUID id, HelpdeskTicketUpdateRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));

        entity.setCategoryId(request.categoryId());
        entity.setUnitId(request.unitId());
        entity.setSubject(request.subject());
        entity.setDescription(request.description());
        entity.setPriority(request.priority());

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_UPDATED", ENTITY_HELPDESK_TICKET, saved.getId(), null);
        return toTicketResponse(saved);
    }

    public void deleteTicket(UUID id, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));

        entity.setDeleted(true);
        helpdeskTicketRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_DELETED", ENTITY_HELPDESK_TICKET, entity.getId(), null);
    }

    public HelpdeskTicketResponse assignTicket(UUID id, HelpdeskTicketAssignRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));

        entity.setAssignedTo(request.assignedTo());
        entity.setAssignedAt(Instant.now());
        entity.setStatus(TicketStatus.IN_PROGRESS);
        entity.setClosedAt(null);

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_ASSIGNED", ENTITY_HELPDESK_TICKET, saved.getId(), null);
        return toTicketResponse(saved);
    }

    public HelpdeskTicketResponse resolveTicket(UUID id, HelpdeskTicketResolveRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));

        entity.setStatus(TicketStatus.RESOLVED);
        entity.setResolvedAt(Instant.now());
        entity.setResolutionNotes(request.resolutionNotes());
        entity.setClosedAt(null);

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_RESOLVED", ENTITY_HELPDESK_TICKET, saved.getId(), null);
        return toTicketResponse(saved);
    }

    public HelpdeskTicketResponse closeTicket(UUID id, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));

        if (entity.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Helpdesk ticket is already closed");
        }

        entity.setStatus(TicketStatus.CLOSED);
        entity.setClosedAt(Instant.now());
        if (entity.getResolvedAt() == null) {
            entity.setResolvedAt(entity.getClosedAt());
        }

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_CLOSED", ENTITY_HELPDESK_TICKET, saved.getId(), null);
        return toTicketResponse(saved);
    }

    public HelpdeskTicketResponse reopenTicket(UUID id, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));

        if (entity.getStatus() != TicketStatus.CLOSED && entity.getStatus() != TicketStatus.RESOLVED) {
            throw new BadRequestException("Only resolved or closed tickets can be reopened");
        }

        entity.setStatus(entity.getAssignedTo() != null ? TicketStatus.IN_PROGRESS : TicketStatus.OPEN);
        entity.setClosedAt(null);

        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_REOPENED", ENTITY_HELPDESK_TICKET, saved.getId(), null);
        return toTicketResponse(saved);
    }

    public HelpdeskTicketResponse rateTicket(UUID id, HelpdeskTicketRateRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity entity = helpdeskTicketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + id));

        if (entity.getStatus() != TicketStatus.RESOLVED && entity.getStatus() != TicketStatus.CLOSED) {
            throw new BadRequestException("Only resolved or closed tickets can be rated");
        }

        entity.setSatisfactionRating(request.satisfactionRating());
        HelpdeskTicketEntity saved = helpdeskTicketRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_RATED", ENTITY_HELPDESK_TICKET, saved.getId(), null);
        return toTicketResponse(saved);
    }

    @Transactional(readOnly = true)
    public HelpdeskTicketStatsResponse statistics() {
        List<HelpdeskTicketEntity> tickets = helpdeskTicketRepository.findAllByDeletedFalse();

        long openTickets = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgressTickets = tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolvedTickets = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closedTickets = tickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();

        Map<UUID, Integer> categorySlaHours = new HashMap<>();
        Instant now = Instant.now();
        long overdueTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.IN_PROGRESS)
                .filter(ticket -> ticket.getCreatedAt() != null && ticket.getCategoryId() != null)
                .filter(ticket -> {
                    Integer slaHours = categorySlaHours.computeIfAbsent(ticket.getCategoryId(), categoryId ->
                            helpdeskCategoryRepository.findByIdAndDeletedFalse(categoryId)
                                    .map(HelpdeskCategoryEntity::getSlaHours)
                                    .orElse(null));
                    if (slaHours == null || slaHours <= 0) {
                        return false;
                    }
                    return ticket.getCreatedAt().plusSeconds((long) slaHours * 3600L).isBefore(now);
                })
                .count();

        BigDecimal averageRating = tickets.stream()
                .map(HelpdeskTicketEntity::getSatisfactionRating)
                .filter(rating -> rating != null)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long ratedCount = tickets.stream().filter(t -> t.getSatisfactionRating() != null).count();
        BigDecimal rating = ratedCount == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : averageRating.divide(BigDecimal.valueOf(ratedCount), 2, RoundingMode.HALF_UP);

        return new HelpdeskTicketStatsResponse(
                tickets.size(),
                openTickets,
                inProgressTickets,
                resolvedTickets,
                closedTickets,
                overdueTickets,
                rating);
    }

    public HelpdeskCommentResponse addComment(UUID ticketId, HelpdeskCommentCreateRequest request, ShieldPrincipal principal) {
        HelpdeskTicketEntity ticket = helpdeskTicketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + ticketId));

        HelpdeskCommentEntity comment = new HelpdeskCommentEntity();
        comment.setTenantId(ticket.getTenantId());
        comment.setTicketId(ticketId);
        comment.setUserId(principal.userId());
        comment.setComment(request.comment());
        comment.setInternalNote(request.internalNote());

        HelpdeskCommentEntity saved = helpdeskCommentRepository.save(comment);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_COMMENT_ADDED", "helpdesk_comment", saved.getId(), null);
        return toCommentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskCommentResponse> listComments(UUID ticketId, Pageable pageable) {
        return PagedResponse.from(helpdeskCommentRepository.findAllByTicketIdAndDeletedFalse(ticketId, pageable).map(this::toCommentResponse));
    }

    public HelpdeskTicketAttachmentResponse addAttachment(
            UUID ticketId,
            HelpdeskTicketAttachmentCreateRequest request,
            ShieldPrincipal principal) {
        HelpdeskTicketEntity ticket = helpdeskTicketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(HELPDESK_TICKET_NOT_FOUND_PREFIX + ticketId));

        HelpdeskTicketAttachmentEntity attachment = new HelpdeskTicketAttachmentEntity();
        attachment.setTenantId(ticket.getTenantId());
        attachment.setTicketId(ticketId);
        attachment.setFileName(request.fileName());
        attachment.setFileUrl(request.fileUrl());
        attachment.setUploadedBy(principal.userId());
        attachment.setUploadedAt(Instant.now());

        HelpdeskTicketAttachmentEntity saved = helpdeskTicketAttachmentRepository.save(attachment);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_ATTACHMENT_ADDED", "helpdesk_ticket_attachment", saved.getId(), null);
        return toAttachmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HelpdeskTicketAttachmentResponse> listAttachments(UUID ticketId, Pageable pageable) {
        return PagedResponse.from(helpdeskTicketAttachmentRepository.findAllByTicketIdAndDeletedFalse(ticketId, pageable)
                .map(this::toAttachmentResponse));
    }

    public void deleteAttachment(UUID attachmentId, ShieldPrincipal principal) {
        HelpdeskTicketAttachmentEntity attachment = helpdeskTicketAttachmentRepository.findByIdAndDeletedFalse(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Helpdesk ticket attachment not found: " + attachmentId));

        attachment.setDeleted(true);
        helpdeskTicketAttachmentRepository.save(attachment);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "HELPDESK_TICKET_ATTACHMENT_DELETED", "helpdesk_ticket_attachment", attachment.getId(), null);
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
                entity.getClosedAt(),
                entity.getResolutionNotes(),
                entity.getSatisfactionRating());
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

    private HelpdeskTicketAttachmentResponse toAttachmentResponse(HelpdeskTicketAttachmentEntity entity) {
        return new HelpdeskTicketAttachmentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTicketId(),
                entity.getFileName(),
                entity.getFileUrl(),
                entity.getUploadedBy(),
                entity.getUploadedAt());
    }
}
