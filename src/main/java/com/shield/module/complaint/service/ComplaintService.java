package com.shield.module.complaint.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
import com.shield.module.complaint.dto.ComplaintAssignRequest;
import com.shield.module.complaint.dto.ComplaintCommentCreateRequest;
import com.shield.module.complaint.dto.ComplaintCommentResponse;
import com.shield.module.complaint.dto.ComplaintCommentUpdateRequest;
import com.shield.module.complaint.dto.ComplaintCreateRequest;
import com.shield.module.complaint.dto.ComplaintResolveRequest;
import com.shield.module.complaint.dto.ComplaintResponse;
import com.shield.module.complaint.dto.ComplaintStatisticsResponse;
import com.shield.module.complaint.dto.ComplaintUpdateRequest;
import com.shield.module.complaint.entity.ComplaintCommentEntity;
import com.shield.module.complaint.entity.ComplaintEntity;
import com.shield.module.complaint.entity.ComplaintPriority;
import com.shield.module.complaint.entity.ComplaintStatus;
import com.shield.module.complaint.repository.ComplaintCommentRepository;
import com.shield.module.complaint.repository.ComplaintRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintService {

    private static final String ENTITY_COMPLAINT = "complaint";
    private static final String ENTITY_COMPLAINT_COMMENT = "complaint_comment";

    private final ComplaintRepository complaintRepository;
    private final ComplaintCommentRepository complaintCommentRepository;
    private final AuditLogService auditLogService;

    public ComplaintResponse create(ComplaintCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();

        ComplaintEntity entity = new ComplaintEntity();
        entity.setTenantId(principal.tenantId());
        entity.setComplaintNumber(generateComplaintNumber());
        entity.setAssetId(request.assetId());
        entity.setRaisedBy(principal.userId());
        entity.setUnitId(request.unitId());
        entity.setTitle(request.title().trim());
        entity.setDescription(request.description().trim());
        entity.setComplaintType(trimToNull(request.complaintType()));
        entity.setLocation(trimToNull(request.location()));
        entity.setPriority(request.priority());
        entity.setStatus(ComplaintStatus.OPEN);
        entity.setSlaHours(sanitizeSlaHours(request.slaHours()));
        entity.setSlaBreach(false);

        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_CREATED", ENTITY_COMPLAINT, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> list(Pageable pageable) {
        return PagedResponse.from(complaintRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public ComplaintResponse update(UUID id, ComplaintUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintEntity entity = findById(id);

        entity.setAssetId(request.assetId());
        entity.setUnitId(request.unitId());
        entity.setTitle(request.title().trim());
        entity.setDescription(request.description().trim());
        entity.setComplaintType(trimToNull(request.complaintType()));
        entity.setLocation(trimToNull(request.location()));
        entity.setPriority(request.priority());
        entity.setSlaHours(sanitizeSlaHours(request.slaHours()));

        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_UPDATED", ENTITY_COMPLAINT, saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintEntity entity = findById(id);
        entity.setDeleted(true);
        complaintRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_DELETED", ENTITY_COMPLAINT, id, null);
    }

    public ComplaintResponse assign(UUID id, ComplaintAssignRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintEntity entity = findById(id);

        entity.setAssignedTo(request.assignedTo());
        entity.setAssignedAt(Instant.now());
        entity.setStatus(ComplaintStatus.ASSIGNED);

        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_ASSIGNED", ENTITY_COMPLAINT, saved.getId(), null);
        return toResponse(saved);
    }

    public ComplaintResponse resolve(UUID id, ComplaintResolveRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintEntity entity = findById(id);

        entity.setStatus(ComplaintStatus.RESOLVED);
        entity.setResolvedAt(Instant.now());
        entity.setResolutionNotes(request == null ? null : trimToNull(request.resolutionNotes()));
        updateSlaBreachFlag(entity);

        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_RESOLVED", ENTITY_COMPLAINT, saved.getId(), null);
        return toResponse(saved);
    }

    public ComplaintResponse close(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintEntity entity = findById(id);

        if (entity.getResolvedAt() == null) {
            entity.setResolvedAt(Instant.now());
        }
        entity.setStatus(ComplaintStatus.CLOSED);
        entity.setClosedAt(Instant.now());
        updateSlaBreachFlag(entity);

        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_CLOSED", ENTITY_COMPLAINT, saved.getId(), null);
        return toResponse(saved);
    }

    public ComplaintResponse reopen(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintEntity entity = findById(id);

        entity.setStatus(ComplaintStatus.OPEN);
        entity.setResolvedAt(null);
        entity.setClosedAt(null);
        entity.setResolutionNotes(null);
        entity.setSlaBreach(isPastSlaDeadline(entity, Instant.now()));

        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_REOPENED", ENTITY_COMPLAINT, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> listByStatus(ComplaintStatus status, Pageable pageable) {
        return PagedResponse.from(complaintRepository.findAllByStatusAndDeletedFalse(status, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> listByPriority(ComplaintPriority priority, Pageable pageable) {
        return PagedResponse.from(complaintRepository.findAllByPriorityAndDeletedFalse(priority, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> listByAsset(UUID assetId, Pageable pageable) {
        return PagedResponse.from(complaintRepository.findAllByAssetIdAndDeletedFalse(assetId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> listMyComplaints(UUID userId, Pageable pageable) {
        return PagedResponse.from(complaintRepository.findAllByRaisedByAndDeletedFalse(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> listAssignedToMe(UUID userId, Pageable pageable) {
        return PagedResponse.from(complaintRepository.findAllByAssignedToAndDeletedFalse(userId, pageable).map(this::toResponse));
    }

    public PagedResponse<ComplaintResponse> listSlaBreached(Pageable pageable) {
        refreshSlaFlags();
        return PagedResponse.from(complaintRepository.findAllBySlaBreachTrueAndDeletedFalse(pageable).map(this::toResponse));
    }

    public ComplaintStatisticsResponse statistics() {
        refreshSlaFlags();
        List<ComplaintEntity> complaints = complaintRepository.findAllByDeletedFalse();

        long open = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.OPEN).count();
        long assigned = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED).count();
        long inProgress = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count();
        long resolved = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count();
        long closed = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.CLOSED).count();
        long slaBreached = complaints.stream().filter(ComplaintEntity::isSlaBreach).count();

        return new ComplaintStatisticsResponse(
                complaints.size(),
                open,
                assigned,
                inProgress,
                resolved,
                closed,
                slaBreached);
    }

    public ComplaintCommentResponse addComment(UUID complaintId, ComplaintCommentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintEntity complaint = findById(complaintId);

        ComplaintCommentEntity entity = new ComplaintCommentEntity();
        entity.setTenantId(complaint.getTenantId());
        entity.setComplaintId(complaintId);
        entity.setUserId(principal.userId());
        entity.setComment(request.comment().trim());

        ComplaintCommentEntity saved = complaintCommentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_COMMENT_CREATED", ENTITY_COMPLAINT_COMMENT, saved.getId(), null);
        return toCommentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintCommentResponse> listComments(UUID complaintId, Pageable pageable) {
        findById(complaintId);
        return PagedResponse.from(complaintCommentRepository.findAllByComplaintIdAndDeletedFalse(complaintId, pageable)
                .map(this::toCommentResponse));
    }

    public ComplaintCommentResponse updateComment(UUID id, ComplaintCommentUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintCommentEntity entity = findCommentById(id);
        entity.setComment(request.comment().trim());
        ComplaintCommentEntity saved = complaintCommentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_COMMENT_UPDATED", ENTITY_COMPLAINT_COMMENT, saved.getId(), null);
        return toCommentResponse(saved);
    }

    public void deleteComment(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        ComplaintCommentEntity entity = findCommentById(id);
        entity.setDeleted(true);
        complaintCommentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "COMPLAINT_COMMENT_DELETED", ENTITY_COMPLAINT_COMMENT, id, null);
    }

    private ComplaintEntity findById(UUID id) {
        return complaintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
    }

    private ComplaintCommentEntity findCommentById(UUID id) {
        return complaintCommentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint comment not found: " + id));
    }

    private String generateComplaintNumber() {
        return "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Integer sanitizeSlaHours(Integer slaHours) {
        if (slaHours == null || slaHours <= 0) {
            return null;
        }
        return slaHours;
    }

    private void refreshSlaFlags() {
        List<ComplaintEntity> complaints = complaintRepository.findAllByDeletedFalse();
        Instant now = Instant.now();
        for (ComplaintEntity complaint : complaints) {
            boolean shouldBeBreached = shouldMarkSlaBreach(complaint, now);
            if (complaint.isSlaBreach() != shouldBeBreached) {
                complaint.setSlaBreach(shouldBeBreached);
                complaintRepository.save(complaint);
            }
        }
    }

    private void updateSlaBreachFlag(ComplaintEntity complaint) {
        complaint.setSlaBreach(shouldMarkSlaBreach(complaint, Instant.now()));
    }

    private boolean shouldMarkSlaBreach(ComplaintEntity complaint, Instant referenceTime) {
        if (!isPastSlaDeadline(complaint, referenceTime)) {
            return false;
        }

        if (complaint.getResolvedAt() == null) {
            return true;
        }

        Instant deadline = complaint.getCreatedAt().plusSeconds(complaint.getSlaHours().longValue() * 3600L);
        return complaint.getResolvedAt().isAfter(deadline);
    }

    private boolean isPastSlaDeadline(ComplaintEntity complaint, Instant referenceTime) {
        if (complaint.getSlaHours() == null || complaint.getCreatedAt() == null) {
            return false;
        }
        Instant deadline = complaint.getCreatedAt().plusSeconds(complaint.getSlaHours().longValue() * 3600L);
        return referenceTime.isAfter(deadline);
    }

    private ComplaintResponse toResponse(ComplaintEntity entity) {
        return new ComplaintResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getComplaintNumber(),
                entity.getAssetId(),
                entity.getRaisedBy(),
                entity.getUnitId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getComplaintType(),
                entity.getLocation(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getAssignedTo(),
                entity.getAssignedAt(),
                entity.getResolvedAt(),
                entity.getResolutionNotes(),
                entity.getClosedAt(),
                entity.getSlaHours(),
                entity.isSlaBreach(),
                entity.getCreatedAt());
    }

    private ComplaintCommentResponse toCommentResponse(ComplaintCommentEntity entity) {
        return new ComplaintCommentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getComplaintId(),
                entity.getUserId(),
                entity.getComment(),
                entity.getCreatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
