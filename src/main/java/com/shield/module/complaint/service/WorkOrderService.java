package com.shield.module.complaint.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
import com.shield.module.complaint.dto.WorkOrderCreateRequest;
import com.shield.module.complaint.dto.WorkOrderResponse;
import com.shield.module.complaint.dto.WorkOrderUpdateRequest;
import com.shield.module.complaint.entity.WorkOrderEntity;
import com.shield.module.complaint.entity.WorkOrderStatus;
import com.shield.module.complaint.repository.ComplaintRepository;
import com.shield.module.complaint.repository.WorkOrderRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ComplaintRepository complaintRepository;
    private final AuditLogService auditLogService;

    public WorkOrderResponse create(WorkOrderCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        complaintRepository.findByIdAndDeletedFalse(request.complaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + request.complaintId()));

        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setTenantId(principal.tenantId());
        entity.setWorkOrderNumber("WO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setComplaintId(request.complaintId());
        entity.setAssetId(request.assetId());
        entity.setVendorId(request.vendorId());
        entity.setWorkDescription(request.workDescription().trim());
        entity.setEstimatedCost(request.estimatedCost());
        entity.setScheduledDate(request.scheduledDate());
        entity.setStatus(WorkOrderStatus.PENDING);
        entity.setCreatedBy(principal.userId());

        WorkOrderEntity saved = workOrderRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "WORK_ORDER_CREATED", "work_order", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkOrderResponse> list(Pageable pageable) {
        return PagedResponse.from(workOrderRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public WorkOrderResponse update(UUID id, WorkOrderUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        WorkOrderEntity entity = findById(id);
        entity.setAssetId(request.assetId());
        entity.setVendorId(request.vendorId());
        entity.setWorkDescription(request.workDescription().trim());
        entity.setEstimatedCost(request.estimatedCost());
        entity.setActualCost(request.actualCost());
        entity.setScheduledDate(request.scheduledDate());
        entity.setCompletionDate(request.completionDate());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        WorkOrderEntity saved = workOrderRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "WORK_ORDER_UPDATED", "work_order", saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        WorkOrderEntity entity = findById(id);
        entity.setDeleted(true);
        workOrderRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "WORK_ORDER_DELETED", "work_order", entity.getId(), null);
    }

    public WorkOrderResponse start(UUID id) {
        return changeStatus(id, WorkOrderStatus.IN_PROGRESS, "WORK_ORDER_STARTED", false);
    }

    public WorkOrderResponse complete(UUID id) {
        return changeStatus(id, WorkOrderStatus.COMPLETED, "WORK_ORDER_COMPLETED", true);
    }

    public WorkOrderResponse cancel(UUID id) {
        return changeStatus(id, WorkOrderStatus.CANCELLED, "WORK_ORDER_CANCELLED", false);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkOrderResponse> listByComplaint(UUID complaintId, Pageable pageable) {
        return PagedResponse.from(workOrderRepository.findAllByComplaintIdAndDeletedFalse(complaintId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkOrderResponse> listByVendor(UUID vendorId, Pageable pageable) {
        return PagedResponse.from(workOrderRepository.findAllByVendorIdAndDeletedFalse(vendorId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkOrderResponse> listByStatus(WorkOrderStatus status, Pageable pageable) {
        return PagedResponse.from(workOrderRepository.findAllByStatusAndDeletedFalse(status, pageable).map(this::toResponse));
    }

    private WorkOrderResponse changeStatus(UUID id, WorkOrderStatus status, String auditAction, boolean setCompletionDate) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        WorkOrderEntity entity = findById(id);
        entity.setStatus(status);
        if (setCompletionDate && entity.getCompletionDate() == null) {
            entity.setCompletionDate(LocalDate.now());
        }
        WorkOrderEntity saved = workOrderRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), auditAction, "work_order", saved.getId(), null);
        return toResponse(saved);
    }

    private WorkOrderEntity findById(UUID id) {
        return workOrderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + id));
    }

    private WorkOrderResponse toResponse(WorkOrderEntity entity) {
        return new WorkOrderResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkOrderNumber(),
                entity.getComplaintId(),
                entity.getAssetId(),
                entity.getVendorId(),
                entity.getWorkDescription(),
                entity.getEstimatedCost(),
                entity.getActualCost(),
                entity.getScheduledDate(),
                entity.getCompletionDate(),
                entity.getStatus(),
                entity.getCreatedBy());
    }
}
