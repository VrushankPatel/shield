package com.shield.module.payroll.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.payroll.dto.PayrollComponentCreateRequest;
import com.shield.module.payroll.dto.PayrollComponentResponse;
import com.shield.module.payroll.dto.PayrollComponentUpdateRequest;
import com.shield.module.payroll.entity.PayrollComponentEntity;
import com.shield.module.payroll.repository.PayrollComponentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayrollComponentService {

    private static final String ENTITY_PAYROLL_COMPONENT = "payroll_component";
    private static final String PAYROLL_COMPONENT_NOT_FOUND_PREFIX = "Payroll component not found: ";

    private final PayrollComponentRepository payrollComponentRepository;
    private final AuditLogService auditLogService;

    public PayrollComponentService(
            PayrollComponentRepository payrollComponentRepository,
            AuditLogService auditLogService) {
        this.payrollComponentRepository = payrollComponentRepository;
        this.auditLogService = auditLogService;
    }

    public PayrollComponentResponse create(PayrollComponentCreateRequest request, ShieldPrincipal principal) {
        String normalizedName = request.componentName().trim();
        if (payrollComponentRepository.existsByComponentNameIgnoreCaseAndDeletedFalse(normalizedName)) {
            throw new BadRequestException("Payroll component name already exists");
        }

        PayrollComponentEntity entity = new PayrollComponentEntity();
        entity.setTenantId(principal.tenantId());
        entity.setComponentName(normalizedName);
        entity.setComponentType(request.componentType());
        entity.setTaxable(request.taxable());

        PayrollComponentEntity saved = payrollComponentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PAYROLL_COMPONENT_CREATED", ENTITY_PAYROLL_COMPONENT, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PayrollComponentResponse> list(Pageable pageable) {
        return PagedResponse.from(payrollComponentRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PayrollComponentResponse getById(UUID id) {
        PayrollComponentEntity entity = payrollComponentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_COMPONENT_NOT_FOUND_PREFIX + id));
        return toResponse(entity);
    }

    public PayrollComponentResponse update(UUID id, PayrollComponentUpdateRequest request, ShieldPrincipal principal) {
        PayrollComponentEntity entity = payrollComponentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_COMPONENT_NOT_FOUND_PREFIX + id));

        String normalizedName = request.componentName().trim();
        if (payrollComponentRepository.existsByComponentNameIgnoreCaseAndDeletedFalseAndIdNot(normalizedName, id)) {
            throw new BadRequestException("Payroll component name already exists");
        }

        entity.setComponentName(normalizedName);
        entity.setComponentType(request.componentType());
        entity.setTaxable(request.taxable());

        PayrollComponentEntity saved = payrollComponentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PAYROLL_COMPONENT_UPDATED", ENTITY_PAYROLL_COMPONENT, saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        PayrollComponentEntity entity = payrollComponentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_COMPONENT_NOT_FOUND_PREFIX + id));

        entity.setDeleted(true);
        payrollComponentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PAYROLL_COMPONENT_DELETED", ENTITY_PAYROLL_COMPONENT, entity.getId(), null);
    }

    private PayrollComponentResponse toResponse(PayrollComponentEntity entity) {
        return new PayrollComponentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getComponentName(),
                entity.getComponentType(),
                entity.isTaxable());
    }
}
