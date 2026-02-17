package com.shield.module.complaint.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.complaint.dto.ComplaintAssignRequest;
import com.shield.module.complaint.dto.ComplaintCreateRequest;
import com.shield.module.complaint.dto.ComplaintResponse;
import com.shield.module.complaint.entity.ComplaintEntity;
import com.shield.module.complaint.entity.ComplaintStatus;
import com.shield.module.complaint.repository.ComplaintRepository;
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final AuditLogService auditLogService;

    public ComplaintResponse create(ComplaintCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        ComplaintEntity entity = new ComplaintEntity();
        entity.setTenantId(tenantId);
        entity.setAssetId(request.assetId());
        entity.setUnitId(request.unitId());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPriority(request.priority());
        entity.setStatus(ComplaintStatus.OPEN);

        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.record(tenantId, null, "COMPLAINT_CREATED", "complaint", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> list(Pageable pageable) {
        return PagedResponse.from(complaintRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public ComplaintResponse assign(UUID id, ComplaintAssignRequest request) {
        ComplaintEntity entity = complaintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));

        entity.setAssignedTo(request.assignedTo());
        entity.setStatus(ComplaintStatus.ASSIGNED);
        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "COMPLAINT_ASSIGNED", "complaint", saved.getId(), null);
        return toResponse(saved);
    }

    public ComplaintResponse resolve(UUID id) {
        ComplaintEntity entity = complaintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));

        entity.setStatus(ComplaintStatus.RESOLVED);
        entity.setResolvedAt(Instant.now());
        ComplaintEntity saved = complaintRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "COMPLAINT_RESOLVED", "complaint", saved.getId(), null);
        return toResponse(saved);
    }

    private ComplaintResponse toResponse(ComplaintEntity entity) {
        return new ComplaintResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAssetId(),
                entity.getUnitId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getAssignedTo(),
                entity.getResolvedAt());
    }
}
