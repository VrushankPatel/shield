package com.shield.module.visitor.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.visitor.dto.VisitorPassCreateRequest;
import com.shield.module.visitor.dto.VisitorPassResponse;
import com.shield.module.visitor.entity.VisitorPassEntity;
import com.shield.module.visitor.entity.VisitorPassStatus;
import com.shield.module.visitor.repository.VisitorPassRepository;
import com.shield.tenant.context.TenantContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VisitorService {

    private final VisitorPassRepository visitorPassRepository;
    private final AuditLogService auditLogService;

    public VisitorPassResponse createPass(VisitorPassCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        VisitorPassEntity pass = new VisitorPassEntity();
        pass.setTenantId(tenantId);
        pass.setUnitId(request.unitId());
        pass.setVisitorName(request.visitorName());
        pass.setVehicleNumber(request.vehicleNumber());
        pass.setValidFrom(request.validFrom());
        pass.setValidTo(request.validTo());
        pass.setQrCode("VIS-" + UUID.randomUUID());
        pass.setStatus(VisitorPassStatus.PENDING);

        VisitorPassEntity saved = visitorPassRepository.save(pass);
        auditLogService.record(tenantId, null, "VISITOR_PASS_CREATED", "visitor_pass", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VisitorPassResponse getPass(UUID id) {
        VisitorPassEntity pass = visitorPassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor pass not found: " + id));
        return toResponse(pass);
    }

    public VisitorPassResponse approve(UUID id) {
        return updateStatus(id, VisitorPassStatus.APPROVED, "VISITOR_PASS_APPROVED");
    }

    public VisitorPassResponse reject(UUID id) {
        return updateStatus(id, VisitorPassStatus.REJECTED, "VISITOR_PASS_REJECTED");
    }

    private VisitorPassResponse updateStatus(UUID id, VisitorPassStatus status, String action) {
        VisitorPassEntity pass = visitorPassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor pass not found: " + id));

        pass.setStatus(status);
        VisitorPassEntity saved = visitorPassRepository.save(pass);
        auditLogService.record(saved.getTenantId(), null, action, "visitor_pass", saved.getId(), null);
        return toResponse(saved);
    }

    private VisitorPassResponse toResponse(VisitorPassEntity pass) {
        return new VisitorPassResponse(
                pass.getId(),
                pass.getTenantId(),
                pass.getUnitId(),
                pass.getVisitorName(),
                pass.getVehicleNumber(),
                pass.getValidFrom(),
                pass.getValidTo(),
                pass.getQrCode(),
                pass.getStatus());
    }
}
