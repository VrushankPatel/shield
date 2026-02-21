package com.shield.module.accounting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.accounting.dto.LedgerCreateRequest;
import com.shield.module.accounting.dto.LedgerResponse;
import com.shield.module.accounting.dto.LedgerSummaryResponse;
import com.shield.module.accounting.entity.LedgerEntryEntity;
import com.shield.module.accounting.entity.LedgerType;
import com.shield.module.accounting.repository.LedgerEntryRepository;
import com.shield.tenant.context.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AccountingServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private AuditLogService auditLogService;

    private AccountingService accountingService;

    @BeforeEach
    void setUp() {
        accountingService = new AccountingService(ledgerEntryRepository, auditLogService);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createShouldPersistEntryWithTenantAndAudit() {
        UUID tenantId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(ledgerEntryRepository.save(any(LedgerEntryEntity.class))).thenAnswer(invocation -> {
            LedgerEntryEntity entity = invocation.getArgument(0);
            entity.setId(entryId);
            return entity;
        });

        LedgerResponse response = accountingService.create(new LedgerCreateRequest(
                LedgerType.INCOME,
                "Maintenance",
                BigDecimal.valueOf(5000),
                "INV-1001",
                "Monthly maintenance collection",
                LocalDate.of(2026, 2, 21)));

        assertEquals(entryId, response.id());
        assertEquals(tenantId, response.tenantId());
        assertEquals(LedgerType.INCOME, response.type());
        verify(auditLogService).record(eq(tenantId), eq(null), eq("LEDGER_ENTRY_CREATED"), eq("ledger_entry"), eq(entryId), eq(null));
    }

    @Test
    void listShouldReadOnlyCurrentTenantEntries() {
        UUID tenantId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setId(entryId);
        entity.setTenantId(tenantId);
        entity.setType(LedgerType.EXPENSE);
        entity.setCategory("Repair");
        entity.setAmount(BigDecimal.valueOf(1200));
        entity.setReference("EXP-200");
        entity.setDescription("Pump repair");
        entity.setEntryDate(LocalDate.of(2026, 2, 20));

        when(ledgerEntryRepository.findAllByTenantIdAndDeletedFalse(eq(tenantId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1));

        var page = accountingService.list(Pageable.ofSize(10));

        assertEquals(1, page.content().size());
        assertEquals(entryId, page.content().get(0).id());
        assertEquals(tenantId, page.content().get(0).tenantId());
    }

    @Test
    void listShouldFailWhenTenantContextMissing() {
        assertThrows(UnauthorizedException.class, () -> accountingService.list(Pageable.ofSize(5)));
    }

    @Test
    void summaryShouldAggregateIncomeAndExpenseForTenant() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(ledgerEntryRepository.summarizeByTenantId(tenantId)).thenReturn(List.of(
                new Object[]{LedgerType.INCOME, BigDecimal.valueOf(10000)},
                new Object[]{LedgerType.EXPENSE, BigDecimal.valueOf(3500)}));

        LedgerSummaryResponse response = accountingService.summary();

        assertEquals(BigDecimal.valueOf(10000), response.totalIncome());
        assertEquals(BigDecimal.valueOf(3500), response.totalExpense());
        assertEquals(BigDecimal.valueOf(6500), response.balance());
    }

    @Test
    void summaryShouldReturnZeroesWhenNoLedgerDataExists() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(ledgerEntryRepository.summarizeByTenantId(tenantId)).thenReturn(List.of());

        LedgerSummaryResponse response = accountingService.summary();

        assertEquals(BigDecimal.ZERO, response.totalIncome());
        assertEquals(BigDecimal.ZERO, response.totalExpense());
        assertEquals(BigDecimal.ZERO, response.balance());
    }

    @Test
    void summaryShouldFailWhenTenantContextMissing() {
        assertThrows(UnauthorizedException.class, accountingService::summary);
    }
}
