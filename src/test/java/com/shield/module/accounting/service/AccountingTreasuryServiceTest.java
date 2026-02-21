package com.shield.module.accounting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.accounting.dto.AccountHeadCreateRequest;
import com.shield.module.accounting.dto.BudgetCreateRequest;
import com.shield.module.accounting.dto.ExpenseCreateRequest;
import com.shield.module.accounting.dto.FundCategoryCreateRequest;
import com.shield.module.accounting.dto.LedgerEntryCreateRequest;
import com.shield.module.accounting.dto.VendorCreateRequest;
import com.shield.module.accounting.dto.VendorPaymentCreateRequest;
import com.shield.module.accounting.entity.AccountHeadEntity;
import com.shield.module.accounting.entity.AccountHeadType;
import com.shield.module.accounting.entity.BudgetEntity;
import com.shield.module.accounting.entity.ExpenseEntity;
import com.shield.module.accounting.entity.ExpensePaymentStatus;
import com.shield.module.accounting.entity.FundCategoryEntity;
import com.shield.module.accounting.entity.LedgerType;
import com.shield.module.accounting.entity.VendorEntity;
import com.shield.module.accounting.entity.VendorPaymentEntity;
import com.shield.module.accounting.entity.VendorPaymentStatus;
import com.shield.module.accounting.repository.AccountHeadRepository;
import com.shield.module.accounting.repository.BudgetRepository;
import com.shield.module.accounting.repository.ExpenseRepository;
import com.shield.module.accounting.repository.FundCategoryRepository;
import com.shield.module.accounting.repository.LedgerEntryRepository;
import com.shield.module.accounting.repository.VendorPaymentRepository;
import com.shield.module.accounting.repository.VendorRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountingTreasuryServiceTest {

    @Mock
    private AccountHeadRepository accountHeadRepository;
    @Mock
    private FundCategoryRepository fundCategoryRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private VendorRepository vendorRepository;
    @Mock
    private VendorPaymentRepository vendorPaymentRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private AuditLogService auditLogService;

    private AccountingTreasuryService accountingTreasuryService;

    @BeforeEach
    void setUp() {
        accountingTreasuryService = new AccountingTreasuryService(
                accountHeadRepository,
                fundCategoryRepository,
                ledgerEntryRepository,
                expenseRepository,
                vendorRepository,
                vendorPaymentRepository,
                budgetRepository,
                auditLogService);
    }

    @Test
    void createAccountHeadShouldParseType() {
        ShieldPrincipal principal = principal();

        when(accountHeadRepository.save(any(AccountHeadEntity.class))).thenAnswer(invocation -> {
            AccountHeadEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = accountingTreasuryService.createAccountHead(
                new AccountHeadCreateRequest("Maintenance", "EXPENSE", null),
                principal);

        assertNotNull(response.id());
        assertEquals(AccountHeadType.EXPENSE, response.headType());
    }

    @Test
    void createLedgerEntryShouldDeriveTypeFromAccountHead() {
        ShieldPrincipal principal = principal();

        UUID accountHeadId = UUID.randomUUID();
        AccountHeadEntity head = new AccountHeadEntity();
        head.setId(accountHeadId);
        head.setTenantId(principal.tenantId());
        head.setHeadName("Collection");
        head.setHeadType(AccountHeadType.INCOME);

        when(accountHeadRepository.findByIdAndDeletedFalse(accountHeadId)).thenReturn(Optional.of(head));
        when(ledgerEntryRepository.save(any())).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, com.shield.module.accounting.entity.LedgerEntryEntity.class);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = accountingTreasuryService.createLedgerEntry(new LedgerEntryCreateRequest(
                LocalDate.now(),
                accountHeadId,
                null,
                "CREDIT",
                BigDecimal.valueOf(5000),
                null,
                null,
                null,
                null,
                null,
                "Collection posted"), principal);

        assertEquals(LedgerType.INCOME, response.type());
        assertEquals("Collection", response.category());
    }

    @Test
    void createAndApproveExpenseShouldSetPaidStatus() {
        ShieldPrincipal principal = principal();

        UUID accountHeadId = UUID.randomUUID();
        AccountHeadEntity accountHead = new AccountHeadEntity();
        accountHead.setId(accountHeadId);
        accountHead.setTenantId(principal.tenantId());
        accountHead.setHeadName("Repairs");
        accountHead.setHeadType(AccountHeadType.EXPENSE);

        when(accountHeadRepository.findByIdAndDeletedFalse(accountHeadId)).thenReturn(Optional.of(accountHead));
        when(expenseRepository.findByExpenseNumberAndDeletedFalse(any())).thenReturn(Optional.empty());
        when(expenseRepository.save(any(ExpenseEntity.class))).thenAnswer(invocation -> {
            ExpenseEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });

        var created = accountingTreasuryService.createExpense(new ExpenseCreateRequest(
                accountHeadId,
                null,
                null,
                LocalDate.now(),
                BigDecimal.valueOf(3000),
                "Pump repair",
                null,
                null), principal);

        when(expenseRepository.findByIdAndDeletedFalse(created.id())).thenReturn(Optional.of(toExpenseEntity(created)));

        var approved = accountingTreasuryService.approveExpense(created.id(), principal);

        assertEquals(ExpensePaymentStatus.PAID, approved.paymentStatus());
        assertNotNull(approved.approvalDate());
    }

    @Test
    void createVendorPaymentShouldMarkLinkedExpensePaidWhenCompleted() {
        ShieldPrincipal principal = principal();

        UUID vendorId = UUID.randomUUID();
        VendorEntity vendor = new VendorEntity();
        vendor.setId(vendorId);
        vendor.setTenantId(principal.tenantId());
        vendor.setVendorName("ABC");
        vendor.setActive(true);

        UUID expenseId = UUID.randomUUID();
        ExpenseEntity expense = new ExpenseEntity();
        expense.setId(expenseId);
        expense.setTenantId(principal.tenantId());
        expense.setExpenseNumber("EXP-2026-1");
        expense.setAccountHeadId(UUID.randomUUID());
        expense.setExpenseDate(LocalDate.now());
        expense.setAmount(BigDecimal.valueOf(1000));
        expense.setPaymentStatus(ExpensePaymentStatus.PENDING);

        when(vendorRepository.findByIdAndDeletedFalse(vendorId)).thenReturn(Optional.of(vendor));
        when(expenseRepository.findByIdAndDeletedFalse(expenseId)).thenReturn(Optional.of(expense));
        when(vendorPaymentRepository.save(any(VendorPaymentEntity.class))).thenAnswer(invocation -> {
            VendorPaymentEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        when(expenseRepository.save(any(ExpenseEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountingTreasuryService.createVendorPayment(new VendorPaymentCreateRequest(
                vendorId,
                expenseId,
                LocalDate.now(),
                BigDecimal.valueOf(1000),
                "UPI",
                "TXN-1",
                "COMPLETED"), principal);

        assertEquals(VendorPaymentStatus.COMPLETED, response.status());
        assertEquals(ExpensePaymentStatus.PAID, expense.getPaymentStatus());
    }

    @Test
    void budgetVsActualShouldCalculateVariance() {
        ShieldPrincipal principal = principal();
        String financialYear = LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1);

        UUID accountHeadId = UUID.randomUUID();
        AccountHeadEntity accountHead = new AccountHeadEntity();
        accountHead.setId(accountHeadId);
        accountHead.setTenantId(principal.tenantId());
        accountHead.setHeadName("Repairs");
        accountHead.setHeadType(AccountHeadType.EXPENSE);

        BudgetEntity budget = new BudgetEntity();
        budget.setId(UUID.randomUUID());
        budget.setTenantId(principal.tenantId());
        budget.setFinancialYear(financialYear);
        budget.setAccountHeadId(accountHeadId);
        budget.setBudgetedAmount(BigDecimal.valueOf(50000));

        when(accountHeadRepository.findByIdAndDeletedFalse(accountHeadId)).thenReturn(Optional.of(accountHead));
        when(budgetRepository.findAllByFinancialYearAndDeletedFalse(financialYear)).thenReturn(List.of(budget));
        when(expenseRepository.sumAmountByAccountHeadAndStatusAndDateRange(any(), any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(12000));

        var vsActual = accountingTreasuryService.listBudgetVsActual(financialYear);

        assertEquals(1, vsActual.size());
        assertEquals(BigDecimal.valueOf(38000), vsActual.get(0).variance());
    }

    @Test
    void incomeStatementShouldSummarizeIncomeAndExpense() {
        when(ledgerEntryRepository.sumAmountByType(LedgerType.INCOME)).thenReturn(BigDecimal.valueOf(100000));
        when(ledgerEntryRepository.sumAmountByType(LedgerType.EXPENSE)).thenReturn(BigDecimal.valueOf(30000));

        var report = accountingTreasuryService.incomeStatement();

        assertEquals("INCOME_STATEMENT", report.reportType());
        assertEquals(BigDecimal.valueOf(70000), report.total());
    }

    @Test
    void budgetVsActualShouldRejectInvalidFinancialYear() {
        assertThrows(BadRequestException.class, () -> accountingTreasuryService.listBudgetVsActual("2026/2027"));
    }

    @Test
    void createFundCategoryAndVendorShouldReturnPersistedRecords() {
        ShieldPrincipal principal = principal();

        when(fundCategoryRepository.save(any(FundCategoryEntity.class))).thenAnswer(invocation -> {
            FundCategoryEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var fund = accountingTreasuryService.createFundCategory(new FundCategoryCreateRequest(
                "Reserve",
                "Reserve bucket",
                BigDecimal.valueOf(1000)), principal);

        assertEquals("Reserve", fund.categoryName());

        when(vendorRepository.save(any(VendorEntity.class))).thenAnswer(invocation -> {
            VendorEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var vendor = accountingTreasuryService.createVendor(new VendorCreateRequest(
                "ABC Services",
                "Raj",
                "9999999999",
                "abc@vendor.com",
                null,
                null,
                null,
                "ELECTRICAL"), principal);

        assertEquals("ABC Services", vendor.vendorName());
        assertEquals(true, vendor.active());
    }

    @Test
    void createBudgetShouldPersist() {
        ShieldPrincipal principal = principal();

        UUID accountHeadId = UUID.randomUUID();
        AccountHeadEntity accountHead = new AccountHeadEntity();
        accountHead.setId(accountHeadId);
        accountHead.setTenantId(principal.tenantId());
        accountHead.setHeadName("Repairs");
        accountHead.setHeadType(AccountHeadType.EXPENSE);

        when(accountHeadRepository.findByIdAndDeletedFalse(accountHeadId)).thenReturn(Optional.of(accountHead));
        when(budgetRepository.save(any(BudgetEntity.class))).thenAnswer(invocation -> {
            BudgetEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = accountingTreasuryService.createBudget(new BudgetCreateRequest(
                "2026-2027",
                accountHeadId,
                BigDecimal.valueOf(250000)), principal);

        assertNotNull(response.id());
        assertEquals(accountHeadId, response.accountHeadId());
    }

    private ShieldPrincipal principal() {
        return new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
    }

    private ExpenseEntity toExpenseEntity(com.shield.module.accounting.dto.ExpenseResponse response) {
        ExpenseEntity entity = new ExpenseEntity();
        entity.setId(response.id());
        entity.setTenantId(response.tenantId());
        entity.setExpenseNumber(response.expenseNumber());
        entity.setAccountHeadId(response.accountHeadId());
        entity.setFundCategoryId(response.fundCategoryId());
        entity.setVendorId(response.vendorId());
        entity.setExpenseDate(response.expenseDate());
        entity.setAmount(response.amount());
        entity.setDescription(response.description());
        entity.setInvoiceNumber(response.invoiceNumber());
        entity.setInvoiceUrl(response.invoiceUrl());
        entity.setPaymentStatus(response.paymentStatus());
        entity.setApprovedBy(response.approvedBy());
        entity.setApprovalDate(response.approvalDate());
        return entity;
    }
}
