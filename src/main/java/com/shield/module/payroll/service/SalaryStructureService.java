package com.shield.module.payroll.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.payroll.dto.SalaryStructureCreateRequest;
import com.shield.module.payroll.dto.SalaryStructureResponse;
import com.shield.module.payroll.dto.SalaryStructureUpdateRequest;
import com.shield.module.payroll.entity.PayrollComponentEntity;
import com.shield.module.payroll.entity.StaffSalaryStructureEntity;
import com.shield.module.payroll.repository.PayrollComponentRepository;
import com.shield.module.payroll.repository.StaffSalaryStructureRepository;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SalaryStructureService {

    private final StaffSalaryStructureRepository staffSalaryStructureRepository;
    private final PayrollComponentRepository payrollComponentRepository;
    private final StaffRepository staffRepository;
    private final AuditLogService auditLogService;

    public SalaryStructureService(
            StaffSalaryStructureRepository staffSalaryStructureRepository,
            PayrollComponentRepository payrollComponentRepository,
            StaffRepository staffRepository,
            AuditLogService auditLogService) {
        this.staffSalaryStructureRepository = staffSalaryStructureRepository;
        this.payrollComponentRepository = payrollComponentRepository;
        this.staffRepository = staffRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<SalaryStructureResponse> listByStaff(UUID staffId, Pageable pageable) {
        if (staffRepository.findByIdAndDeletedFalse(staffId).isEmpty()) {
            throw new ResourceNotFoundException("Staff not found: " + staffId);
        }

        var page = staffSalaryStructureRepository.findAllByStaffIdAndDeletedFalse(staffId, pageable);
        Map<UUID, PayrollComponentEntity> componentsById = loadComponents(page.getContent().stream()
                .map(StaffSalaryStructureEntity::getPayrollComponentId)
                .collect(Collectors.toSet()));

        return PagedResponse.from(page.map(entity -> toResponse(entity, componentsById.get(entity.getPayrollComponentId()))));
    }

    public SalaryStructureResponse create(UUID staffId, SalaryStructureCreateRequest request, ShieldPrincipal principal) {
        requireStaff(staffId);
        PayrollComponentEntity component = requireComponent(request.payrollComponentId());

        if (staffSalaryStructureRepository.existsByStaffIdAndPayrollComponentIdAndEffectiveFromAndDeletedFalse(
                staffId,
                request.payrollComponentId(),
                request.effectiveFrom())) {
            throw new BadRequestException("Salary structure for this component and effectiveFrom already exists");
        }

        StaffSalaryStructureEntity entity = new StaffSalaryStructureEntity();
        entity.setTenantId(principal.tenantId());
        entity.setStaffId(staffId);
        entity.setPayrollComponentId(request.payrollComponentId());
        entity.setAmount(request.amount());
        entity.setActive(request.active());
        entity.setEffectiveFrom(request.effectiveFrom());

        StaffSalaryStructureEntity saved = staffSalaryStructureRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SALARY_STRUCTURE_CREATED", "staff_salary_structure", saved.getId(), null);
        return toResponse(saved, component);
    }

    public SalaryStructureResponse update(UUID id, SalaryStructureUpdateRequest request, ShieldPrincipal principal) {
        StaffSalaryStructureEntity entity = staffSalaryStructureRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found: " + id));

        requireStaff(entity.getStaffId());
        PayrollComponentEntity component = requireComponent(request.payrollComponentId());

        if (staffSalaryStructureRepository.existsByStaffIdAndPayrollComponentIdAndEffectiveFromAndDeletedFalseAndIdNot(
                entity.getStaffId(),
                request.payrollComponentId(),
                request.effectiveFrom(),
                id)) {
            throw new BadRequestException("Salary structure for this component and effectiveFrom already exists");
        }

        entity.setPayrollComponentId(request.payrollComponentId());
        entity.setAmount(request.amount());
        entity.setActive(request.active());
        entity.setEffectiveFrom(request.effectiveFrom());

        StaffSalaryStructureEntity saved = staffSalaryStructureRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SALARY_STRUCTURE_UPDATED", "staff_salary_structure", saved.getId(), null);
        return toResponse(saved, component);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        StaffSalaryStructureEntity entity = staffSalaryStructureRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found: " + id));

        entity.setDeleted(true);
        staffSalaryStructureRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SALARY_STRUCTURE_DELETED", "staff_salary_structure", entity.getId(), null);
    }

    private void requireStaff(UUID staffId) {
        if (staffRepository.findByIdAndDeletedFalse(staffId).isEmpty()) {
            throw new ResourceNotFoundException("Staff not found: " + staffId);
        }
    }

    private PayrollComponentEntity requireComponent(UUID componentId) {
        return payrollComponentRepository.findByIdAndDeletedFalse(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll component not found: " + componentId));
    }

    private Map<UUID, PayrollComponentEntity> loadComponents(Set<UUID> componentIds) {
        return payrollComponentRepository.findAllByIdInAndDeletedFalse(componentIds).stream()
                .collect(Collectors.toMap(PayrollComponentEntity::getId, Function.identity()));
    }

    private SalaryStructureResponse toResponse(StaffSalaryStructureEntity entity, PayrollComponentEntity component) {
        String componentName = component == null ? null : component.getComponentName();
        var componentType = component == null ? null : component.getComponentType();
        return new SalaryStructureResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getStaffId(),
                entity.getPayrollComponentId(),
                componentName,
                componentType,
                entity.getAmount(),
                entity.isActive(),
                entity.getEffectiveFrom());
    }
}
