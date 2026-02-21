package com.shield.module.accounting.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.module.accounting.dto.LedgerCreateRequest;
import com.shield.module.accounting.dto.LedgerResponse;
import com.shield.module.accounting.dto.LedgerSummaryResponse;
import com.shield.module.accounting.entity.LedgerEntryEntity;
import com.shield.module.accounting.entity.LedgerType;
import com.shield.module.accounting.repository.LedgerEntryRepository;
import com.shield.tenant.context.TenantContext;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountingService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditLogService auditLogService;

    public LedgerResponse create(LedgerCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setTenantId(tenantId);
        entity.setType(request.type());
        entity.setCategory(request.category());
        entity.setAmount(request.amount());
        entity.setReference(request.reference());
        entity.setDescription(request.description());
        entity.setEntryDate(request.entryDate());

        LedgerEntryEntity saved = ledgerEntryRepository.save(entity);
        auditLogService.logEvent(tenantId, null, "LEDGER_ENTRY_CREATED", "ledger_entry", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(ledgerEntryRepository.findAllByTenantIdAndDeletedFalse(tenantId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public LedgerSummaryResponse summary() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Object[] row : ledgerEntryRepository.summarizeByTenantId(tenantId)) {
            LedgerType type = (LedgerType) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (type == LedgerType.INCOME) {
                income = amount;
            } else if (type == LedgerType.EXPENSE) {
                expense = amount;
            }
        }

        return new LedgerSummaryResponse(income, expense, income.subtract(expense));
    }

    private LedgerResponse toResponse(LedgerEntryEntity entity) {
        return new LedgerResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getType(),
                entity.getCategory(),
                entity.getAmount(),
                entity.getReference(),
                entity.getDescription(),
                entity.getEntryDate());
    }
}
