package com.shield.module.accounting.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.accounting.dto.AccountHeadCreateRequest;
import com.shield.module.accounting.dto.AccountHeadResponse;
import com.shield.module.accounting.dto.AccountHeadUpdateRequest;
import com.shield.module.accounting.dto.BudgetCreateRequest;
import com.shield.module.accounting.dto.BudgetResponse;
import com.shield.module.accounting.dto.BudgetUpdateRequest;
import com.shield.module.accounting.dto.BudgetVsActualResponse;
import com.shield.module.accounting.dto.ExpenseCreateRequest;
import com.shield.module.accounting.dto.ExpenseResponse;
import com.shield.module.accounting.dto.ExpenseUpdateRequest;
import com.shield.module.accounting.dto.FinancialReportLine;
import com.shield.module.accounting.dto.FinancialReportResponse;
import com.shield.module.accounting.dto.FundBalanceResponse;
import com.shield.module.accounting.dto.FundCategoryCreateRequest;
import com.shield.module.accounting.dto.FundCategoryResponse;
import com.shield.module.accounting.dto.FundCategoryUpdateRequest;
import com.shield.module.accounting.dto.LedgerEntryBulkCreateRequest;
import com.shield.module.accounting.dto.LedgerEntryCreateRequest;
import com.shield.module.accounting.dto.LedgerEntryResponse;
import com.shield.module.accounting.dto.LedgerEntryUpdateRequest;
import com.shield.module.accounting.dto.VendorCreateRequest;
import com.shield.module.accounting.dto.VendorPaymentCreateRequest;
import com.shield.module.accounting.dto.VendorPaymentResponse;
import com.shield.module.accounting.dto.VendorResponse;
import com.shield.module.accounting.dto.VendorUpdateRequest;
import com.shield.module.accounting.entity.AccountHeadEntity;
import com.shield.module.accounting.entity.AccountHeadType;
import com.shield.module.accounting.entity.BudgetEntity;
import com.shield.module.accounting.entity.ExpenseEntity;
import com.shield.module.accounting.entity.ExpensePaymentStatus;
import com.shield.module.accounting.entity.FundCategoryEntity;
import com.shield.module.accounting.entity.LedgerEntryEntity;
import com.shield.module.accounting.entity.LedgerTransactionType;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountingTreasuryService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AccountHeadRepository accountHeadRepository;
    private final FundCategoryRepository fundCategoryRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final ExpenseRepository expenseRepository;
    private final VendorRepository vendorRepository;
    private final VendorPaymentRepository vendorPaymentRepository;
    private final BudgetRepository budgetRepository;
    private final AuditLogService auditLogService;

    public AccountHeadResponse createAccountHead(AccountHeadCreateRequest request, ShieldPrincipal principal) {
        AccountHeadEntity entity = new AccountHeadEntity();
        entity.setTenantId(principal.tenantId());
        entity.setHeadName(request.headName().trim());
        entity.setHeadType(parseAccountHeadType(request.headType()));
        entity.setParentHeadId(request.parentHeadId());
        AccountHeadEntity saved = accountHeadRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ACCOUNT_HEAD_CREATED", "account_head", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountHeadResponse getAccountHead(UUID id) {
        return toResponse(findAccountHead(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccountHeadResponse> listAccountHeads(Pageable pageable) {
        return PagedResponse.from(accountHeadRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccountHeadResponse> listAccountHeadsByType(String type, Pageable pageable) {
        return PagedResponse.from(accountHeadRepository.findAllByHeadTypeAndDeletedFalse(parseAccountHeadType(type), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<AccountHeadResponse> listAccountHeadHierarchy() {
        return accountHeadRepository.findAllByDeletedFalseOrderByHeadNameAsc().stream().map(this::toResponse).toList();
    }

    public AccountHeadResponse updateAccountHead(UUID id, AccountHeadUpdateRequest request, ShieldPrincipal principal) {
        AccountHeadEntity entity = findAccountHead(id);
        entity.setHeadName(request.headName().trim());
        entity.setHeadType(parseAccountHeadType(request.headType()));
        entity.setParentHeadId(request.parentHeadId());
        AccountHeadEntity saved = accountHeadRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ACCOUNT_HEAD_UPDATED", "account_head", saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteAccountHead(UUID id, ShieldPrincipal principal) {
        AccountHeadEntity entity = findAccountHead(id);
        entity.setDeleted(true);
        accountHeadRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ACCOUNT_HEAD_DELETED", "account_head", id, null);
    }

    public FundCategoryResponse createFundCategory(FundCategoryCreateRequest request, ShieldPrincipal principal) {
        FundCategoryEntity entity = new FundCategoryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setCategoryName(request.categoryName().trim());
        entity.setDescription(trimToNull(request.description()));
        entity.setCurrentBalance(request.currentBalance());
        FundCategoryEntity saved = fundCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "FUND_CATEGORY_CREATED", "fund_category", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FundCategoryResponse getFundCategory(UUID id) {
        return toResponse(findFundCategory(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<FundCategoryResponse> listFundCategories(Pageable pageable) {
        return PagedResponse.from(fundCategoryRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public FundCategoryResponse updateFundCategory(UUID id, FundCategoryUpdateRequest request, ShieldPrincipal principal) {
        FundCategoryEntity entity = findFundCategory(id);
        entity.setCategoryName(request.categoryName().trim());
        entity.setDescription(trimToNull(request.description()));
        entity.setCurrentBalance(request.currentBalance());
        FundCategoryEntity saved = fundCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "FUND_CATEGORY_UPDATED", "fund_category", saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteFundCategory(UUID id, ShieldPrincipal principal) {
        FundCategoryEntity entity = findFundCategory(id);
        entity.setDeleted(true);
        fundCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "FUND_CATEGORY_DELETED", "fund_category", id, null);
    }

    @Transactional(readOnly = true)
    public FundBalanceResponse getFundBalance(UUID id) {
        FundCategoryEntity entity = findFundCategory(id);
        return new FundBalanceResponse(entity.getId(), entity.getCategoryName(), entity.getCurrentBalance());
    }

    @Transactional(readOnly = true)
    public List<FundBalanceResponse> listFundBalances() {
        return fundCategoryRepository.findAllByDeletedFalseOrderByCategoryNameAsc().stream()
                .map(entity -> new FundBalanceResponse(entity.getId(), entity.getCategoryName(), entity.getCurrentBalance()))
                .toList();
    }

    public LedgerEntryResponse createLedgerEntry(LedgerEntryCreateRequest request, ShieldPrincipal principal) {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setEntryDate(request.entryDate());
        entity.setAccountHeadId(request.accountHeadId());
        entity.setFundCategoryId(request.fundCategoryId());
        entity.setTransactionType(parseLedgerTransactionType(request.transactionType()));
        entity.setAmount(request.amount());
        entity.setReference(trimToNull(request.reference()));
        entity.setReferenceType(trimToNull(request.referenceType()));
        entity.setReferenceId(request.referenceId());
        entity.setDescription(trimToNull(request.description()));
        entity.setCreatedBy(principal.userId());

        AccountHeadEntity accountHead = null;
        if (request.accountHeadId() != null) {
            accountHead = findAccountHead(request.accountHeadId());
        }
        entity.setType(resolveLedgerType(request.type(), accountHead));
        entity.setCategory(resolveCategory(request.category(), accountHead));

        LedgerEntryEntity saved = ledgerEntryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "LEDGER_ENTRY_CREATED", "ledger_entry", saved.getId(), null);
        return toLedgerEntryResponse(saved);
    }

    public List<LedgerEntryResponse> bulkCreateLedgerEntries(LedgerEntryBulkCreateRequest request, ShieldPrincipal principal) {
        return request.entries().stream().map(entry -> createLedgerEntry(entry, principal)).toList();
    }

    @Transactional(readOnly = true)
    public LedgerEntryResponse getLedgerEntry(UUID id) {
        return toLedgerEntryResponse(findLedgerEntry(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryResponse> listLedgerEntries(Pageable pageable) {
        return PagedResponse.from(ledgerEntryRepository.findAllByDeletedFalse(pageable).map(this::toLedgerEntryResponse));
    }

    public LedgerEntryResponse updateLedgerEntry(UUID id, LedgerEntryUpdateRequest request, ShieldPrincipal principal) {
        LedgerEntryEntity entity = findLedgerEntry(id);
        entity.setEntryDate(request.entryDate());
        entity.setAccountHeadId(request.accountHeadId());
        entity.setFundCategoryId(request.fundCategoryId());
        entity.setTransactionType(parseLedgerTransactionType(request.transactionType()));
        entity.setAmount(request.amount());
        entity.setReference(trimToNull(request.reference()));
        entity.setReferenceType(trimToNull(request.referenceType()));
        entity.setReferenceId(request.referenceId());
        entity.setDescription(trimToNull(request.description()));

        AccountHeadEntity accountHead = null;
        if (request.accountHeadId() != null) {
            accountHead = findAccountHead(request.accountHeadId());
        }
        entity.setType(resolveLedgerType(request.type(), accountHead));
        entity.setCategory(resolveCategory(request.category(), accountHead));

        LedgerEntryEntity saved = ledgerEntryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "LEDGER_ENTRY_UPDATED", "ledger_entry", saved.getId(), null);
        return toLedgerEntryResponse(saved);
    }

    public void deleteLedgerEntry(UUID id, ShieldPrincipal principal) {
        LedgerEntryEntity entity = findLedgerEntry(id);
        entity.setDeleted(true);
        ledgerEntryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "LEDGER_ENTRY_DELETED", "ledger_entry", id, null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryResponse> listLedgerEntriesByAccountHead(UUID accountHeadId, Pageable pageable) {
        return PagedResponse.from(ledgerEntryRepository.findAllByAccountHeadIdAndDeletedFalse(accountHeadId, pageable).map(this::toLedgerEntryResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryResponse> listLedgerEntriesByFundCategory(UUID fundCategoryId, Pageable pageable) {
        return PagedResponse.from(ledgerEntryRepository.findAllByFundCategoryIdAndDeletedFalse(fundCategoryId, pageable).map(this::toLedgerEntryResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryResponse> listLedgerEntriesByDateRange(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        if (fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate must be before or equal to toDate");
        }
        return PagedResponse.from(ledgerEntryRepository.findAllByEntryDateBetweenAndDeletedFalse(fromDate, toDate, pageable).map(this::toLedgerEntryResponse));
    }

    @Transactional(readOnly = true)
    public String exportLedgerEntries() {
        List<LedgerEntryEntity> entries = ledgerEntryRepository.findAllByDeletedFalseOrderByEntryDateDesc();
        StringBuilder builder = new StringBuilder("id,entryDate,type,category,amount,reference,description\n");
        for (LedgerEntryEntity entry : entries) {
            builder.append(entry.getId()).append(',')
                    .append(entry.getEntryDate()).append(',')
                    .append(entry.getType()).append(',')
                    .append(escapeCsv(entry.getCategory())).append(',')
                    .append(entry.getAmount()).append(',')
                    .append(escapeCsv(entry.getReference())).append(',')
                    .append(escapeCsv(entry.getDescription())).append('\n');
        }
        return builder.toString();
    }

    public ExpenseResponse createExpense(ExpenseCreateRequest request, ShieldPrincipal principal) {
        findAccountHead(request.accountHeadId());
        if (request.fundCategoryId() != null) {
            findFundCategory(request.fundCategoryId());
        }
        if (request.vendorId() != null) {
            findVendor(request.vendorId());
        }

        ExpenseEntity entity = new ExpenseEntity();
        entity.setTenantId(principal.tenantId());
        entity.setExpenseNumber(generateExpenseNumber());
        entity.setAccountHeadId(request.accountHeadId());
        entity.setFundCategoryId(request.fundCategoryId());
        entity.setVendorId(request.vendorId());
        entity.setExpenseDate(request.expenseDate());
        entity.setAmount(request.amount());
        entity.setDescription(trimToNull(request.description()));
        entity.setInvoiceNumber(trimToNull(request.invoiceNumber()));
        entity.setInvoiceUrl(trimToNull(request.invoiceUrl()));
        entity.setPaymentStatus(ExpensePaymentStatus.PENDING);

        ExpenseEntity saved = expenseRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EXPENSE_CREATED", "expense", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(UUID id) {
        return toResponse(findExpense(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> listExpenses(Pageable pageable) {
        return PagedResponse.from(expenseRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public ExpenseResponse updateExpense(UUID id, ExpenseUpdateRequest request, ShieldPrincipal principal) {
        findAccountHead(request.accountHeadId());
        if (request.fundCategoryId() != null) {
            findFundCategory(request.fundCategoryId());
        }
        if (request.vendorId() != null) {
            findVendor(request.vendorId());
        }

        ExpenseEntity entity = findExpense(id);
        entity.setAccountHeadId(request.accountHeadId());
        entity.setFundCategoryId(request.fundCategoryId());
        entity.setVendorId(request.vendorId());
        entity.setExpenseDate(request.expenseDate());
        entity.setAmount(request.amount());
        entity.setDescription(trimToNull(request.description()));
        entity.setInvoiceNumber(trimToNull(request.invoiceNumber()));
        entity.setInvoiceUrl(trimToNull(request.invoiceUrl()));
        entity.setPaymentStatus(parseExpensePaymentStatus(request.paymentStatus()));

        ExpenseEntity saved = expenseRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EXPENSE_UPDATED", "expense", saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteExpense(UUID id, ShieldPrincipal principal) {
        ExpenseEntity entity = findExpense(id);
        entity.setDeleted(true);
        expenseRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EXPENSE_DELETED", "expense", id, null);
    }

    public ExpenseResponse approveExpense(UUID id, ShieldPrincipal principal) {
        ExpenseEntity entity = findExpense(id);
        entity.setPaymentStatus(ExpensePaymentStatus.PAID);
        entity.setApprovedBy(principal.userId());
        entity.setApprovalDate(LocalDate.now());
        ExpenseEntity saved = expenseRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EXPENSE_APPROVED", "expense", id, null);
        return toResponse(saved);
    }

    public ExpenseResponse rejectExpense(UUID id, ShieldPrincipal principal) {
        ExpenseEntity entity = findExpense(id);
        entity.setPaymentStatus(ExpensePaymentStatus.REJECTED);
        entity.setApprovedBy(principal.userId());
        entity.setApprovalDate(LocalDate.now());
        ExpenseEntity saved = expenseRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EXPENSE_REJECTED", "expense", id, null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> listPendingExpenses(Pageable pageable) {
        return PagedResponse.from(expenseRepository.findAllByPaymentStatusAndDeletedFalse(ExpensePaymentStatus.PENDING, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> listExpensesByVendor(UUID vendorId, Pageable pageable) {
        return PagedResponse.from(expenseRepository.findAllByVendorIdAndDeletedFalse(vendorId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> listExpensesByAccountHead(UUID accountHeadId, Pageable pageable) {
        return PagedResponse.from(expenseRepository.findAllByAccountHeadIdAndDeletedFalse(accountHeadId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> listExpensesByDateRange(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        if (fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate must be before or equal to toDate");
        }
        return PagedResponse.from(expenseRepository.findAllByExpenseDateBetweenAndDeletedFalse(fromDate, toDate, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public String exportExpenses() {
        List<ExpenseEntity> entries = expenseRepository.findAllByDeletedFalse(Pageable.ofSize(5000)).getContent();
        StringBuilder builder = new StringBuilder("id,expenseNumber,expenseDate,amount,status,vendorId,description\n");
        for (ExpenseEntity entry : entries) {
            builder.append(entry.getId()).append(',')
                    .append(entry.getExpenseNumber()).append(',')
                    .append(entry.getExpenseDate()).append(',')
                    .append(entry.getAmount()).append(',')
                    .append(entry.getPaymentStatus()).append(',')
                    .append(entry.getVendorId() == null ? "" : entry.getVendorId()).append(',')
                    .append(escapeCsv(entry.getDescription())).append('\n');
        }
        return builder.toString();
    }

    public VendorResponse createVendor(VendorCreateRequest request, ShieldPrincipal principal) {
        VendorEntity entity = new VendorEntity();
        entity.setTenantId(principal.tenantId());
        entity.setVendorName(request.vendorName().trim());
        entity.setContactPerson(trimToNull(request.contactPerson()));
        entity.setPhone(trimToNull(request.phone()));
        entity.setEmail(trimToNull(request.email()));
        entity.setAddress(trimToNull(request.address()));
        entity.setGstin(trimToNull(request.gstin()));
        entity.setPan(trimToNull(request.pan()));
        entity.setVendorType(trimToNull(request.vendorType()));
        entity.setActive(true);
        VendorEntity saved = vendorRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "VENDOR_CREATED", "vendor", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(UUID id) {
        return toResponse(findVendor(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorResponse> listVendors(Pageable pageable) {
        return PagedResponse.from(vendorRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public VendorResponse updateVendor(UUID id, VendorUpdateRequest request, ShieldPrincipal principal) {
        VendorEntity entity = findVendor(id);
        entity.setVendorName(request.vendorName().trim());
        entity.setContactPerson(trimToNull(request.contactPerson()));
        entity.setPhone(trimToNull(request.phone()));
        entity.setEmail(trimToNull(request.email()));
        entity.setAddress(trimToNull(request.address()));
        entity.setGstin(trimToNull(request.gstin()));
        entity.setPan(trimToNull(request.pan()));
        entity.setVendorType(trimToNull(request.vendorType()));
        entity.setActive(request.active());
        VendorEntity saved = vendorRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "VENDOR_UPDATED", "vendor", saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteVendor(UUID id, ShieldPrincipal principal) {
        VendorEntity entity = findVendor(id);
        entity.setDeleted(true);
        vendorRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "VENDOR_DELETED", "vendor", id, null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorResponse> listVendorsByType(String vendorType, Pageable pageable) {
        return PagedResponse.from(vendorRepository.findAllByVendorTypeIgnoreCaseAndDeletedFalse(vendorType.trim(), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorResponse> listActiveVendors(Pageable pageable) {
        return PagedResponse.from(vendorRepository.findAllByActiveAndDeletedFalse(true, pageable).map(this::toResponse));
    }

    public VendorResponse updateVendorStatus(UUID id, boolean active, ShieldPrincipal principal) {
        VendorEntity entity = findVendor(id);
        entity.setActive(active);
        VendorEntity saved = vendorRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "VENDOR_STATUS_UPDATED", "vendor", id, null);
        return toResponse(saved);
    }

    public VendorPaymentResponse createVendorPayment(VendorPaymentCreateRequest request, ShieldPrincipal principal) {
        findVendor(request.vendorId());
        ExpenseEntity expense = null;
        if (request.expenseId() != null) {
            expense = findExpense(request.expenseId());
        }

        VendorPaymentEntity entity = new VendorPaymentEntity();
        entity.setTenantId(principal.tenantId());
        entity.setVendorId(request.vendorId());
        entity.setExpenseId(request.expenseId());
        entity.setPaymentDate(request.paymentDate());
        entity.setAmount(request.amount());
        entity.setPaymentMethod(trimToNull(request.paymentMethod()));
        entity.setTransactionReference(trimToNull(request.transactionReference()));
        entity.setCreatedBy(principal.userId());
        entity.setStatus(parseVendorPaymentStatus(request.status()));

        VendorPaymentEntity saved = vendorPaymentRepository.save(entity);

        if (expense != null && saved.getStatus() == VendorPaymentStatus.COMPLETED) {
            expense.setPaymentStatus(ExpensePaymentStatus.PAID);
            expense.setApprovedBy(principal.userId());
            expense.setApprovalDate(LocalDate.now());
            expenseRepository.save(expense);
        }

        auditLogService.record(principal.tenantId(), principal.userId(), "VENDOR_PAYMENT_CREATED", "vendor_payment", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VendorPaymentResponse getVendorPayment(UUID id) {
        return toResponse(findVendorPayment(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorPaymentResponse> listVendorPayments(Pageable pageable) {
        return PagedResponse.from(vendorPaymentRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorPaymentResponse> listVendorPaymentsByVendor(UUID vendorId, Pageable pageable) {
        return PagedResponse.from(vendorPaymentRepository.findAllByVendorIdAndDeletedFalse(vendorId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorPaymentResponse> listVendorPaymentsByExpense(UUID expenseId, Pageable pageable) {
        return PagedResponse.from(vendorPaymentRepository.findAllByExpenseIdAndDeletedFalse(expenseId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorPaymentResponse> listPendingVendorPayments(Pageable pageable) {
        return PagedResponse.from(vendorPaymentRepository.findAllByStatusAndDeletedFalse(VendorPaymentStatus.PENDING, pageable).map(this::toResponse));
    }

    public BudgetResponse createBudget(BudgetCreateRequest request, ShieldPrincipal principal) {
        findAccountHead(request.accountHeadId());

        BudgetEntity entity = new BudgetEntity();
        entity.setTenantId(principal.tenantId());
        entity.setFinancialYear(request.financialYear().trim());
        entity.setAccountHeadId(request.accountHeadId());
        entity.setBudgetedAmount(request.budgetedAmount());
        BudgetEntity saved = budgetRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "BUDGET_CREATED", "budget", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(UUID id) {
        return toResponse(findBudget(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BudgetResponse> listBudgets(Pageable pageable) {
        return PagedResponse.from(budgetRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public BudgetResponse updateBudget(UUID id, BudgetUpdateRequest request, ShieldPrincipal principal) {
        findAccountHead(request.accountHeadId());

        BudgetEntity entity = findBudget(id);
        entity.setFinancialYear(request.financialYear().trim());
        entity.setAccountHeadId(request.accountHeadId());
        entity.setBudgetedAmount(request.budgetedAmount());
        BudgetEntity saved = budgetRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "BUDGET_UPDATED", "budget", saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteBudget(UUID id, ShieldPrincipal principal) {
        BudgetEntity entity = findBudget(id);
        entity.setDeleted(true);
        budgetRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "BUDGET_DELETED", "budget", id, null);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listBudgetsByFinancialYear(String financialYear) {
        return budgetRepository.findAllByFinancialYearAndDeletedFalse(financialYear.trim()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BudgetVsActualResponse> listBudgetVsActual(String financialYear) {
        FinancialYearRange range = resolveFinancialYearRange(financialYear);
        List<BudgetEntity> budgets = budgetRepository.findAllByFinancialYearAndDeletedFalse(range.label());
        List<BudgetVsActualResponse> responses = new ArrayList<>();
        for (BudgetEntity budget : budgets) {
            AccountHeadEntity accountHead = findAccountHead(budget.getAccountHeadId());
            BigDecimal actual = safeAmount(expenseRepository.sumAmountByAccountHeadAndStatusAndDateRange(
                    budget.getAccountHeadId(),
                    ExpensePaymentStatus.PAID,
                    range.from(),
                    range.to()));
            responses.add(new BudgetVsActualResponse(
                    budget.getAccountHeadId(),
                    accountHead.getHeadName(),
                    budget.getFinancialYear(),
                    budget.getBudgetedAmount(),
                    actual,
                    budget.getBudgetedAmount().subtract(actual)));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public FinancialReportResponse incomeStatement(String financialYear) {
        BigDecimal income = safeAmount(ledgerEntryRepository.sumAmountByType(LedgerType.INCOME));
        BigDecimal expense = safeAmount(ledgerEntryRepository.sumAmountByType(LedgerType.EXPENSE));
        BigDecimal net = income.subtract(expense);

        return new FinancialReportResponse(
                "INCOME_STATEMENT",
                List.of(
                        new FinancialReportLine("Total Income", income),
                        new FinancialReportLine("Total Expense", expense),
                        new FinancialReportLine("Net Income", net)),
                net,
                Instant.now());
    }

    @Transactional(readOnly = true)
    public FinancialReportResponse balanceSheet(String financialYear) {
        BigDecimal totalAssets = fundCategoryRepository.findAllByDeletedFalseOrderByCategoryNameAsc().stream()
                .map(FundCategoryEntity::getCurrentBalance)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = safeAmount(expenseRepository
                .sumAmountByStatusAndDateRange(ExpensePaymentStatus.PENDING, LocalDate.of(2000, 1, 1), LocalDate.now().plusYears(20)));

        BigDecimal netPosition = totalAssets.subtract(totalLiabilities);

        return new FinancialReportResponse(
                "BALANCE_SHEET",
                List.of(
                        new FinancialReportLine("Total Assets", totalAssets),
                        new FinancialReportLine("Total Liabilities", totalLiabilities),
                        new FinancialReportLine("Net Position", netPosition)),
                netPosition,
                Instant.now());
    }

    @Transactional(readOnly = true)
    public FinancialReportResponse cashFlow(String financialYear) {
        BigDecimal inflow = safeAmount(ledgerEntryRepository.sumAmountByType(LedgerType.INCOME));
        BigDecimal outflow = safeAmount(vendorPaymentRepository.sumAmountByStatus(VendorPaymentStatus.COMPLETED));
        BigDecimal netFlow = inflow.subtract(outflow);

        return new FinancialReportResponse(
                "CASH_FLOW",
                List.of(
                        new FinancialReportLine("Cash Inflow", inflow),
                        new FinancialReportLine("Cash Outflow", outflow),
                        new FinancialReportLine("Net Cash Flow", netFlow)),
                netFlow,
                Instant.now());
    }

    @Transactional(readOnly = true)
    public FinancialReportResponse trialBalance(String financialYear) {
        List<FinancialReportLine> lines = new ArrayList<>();
        BigDecimal total = ZERO;

        for (AccountHeadEntity accountHead : accountHeadRepository.findAllByDeletedFalseOrderByHeadNameAsc()) {
            BigDecimal amount = safeAmount(ledgerEntryRepository.sumAmountByAccountHeadId(accountHead.getId()));
            lines.add(new FinancialReportLine(accountHead.getHeadName(), amount));
            total = total.add(amount);
        }

        return new FinancialReportResponse("TRIAL_BALANCE", lines, total, Instant.now());
    }

    @Transactional(readOnly = true)
    public FinancialReportResponse fundSummary() {
        List<FinancialReportLine> lines = fundCategoryRepository.findAllByDeletedFalseOrderByCategoryNameAsc().stream()
                .map(fund -> new FinancialReportLine(fund.getCategoryName(), fund.getCurrentBalance()))
                .toList();
        BigDecimal total = lines.stream().map(FinancialReportLine::amount).reduce(ZERO, BigDecimal::add);
        return new FinancialReportResponse("FUND_SUMMARY", lines, total, Instant.now());
    }

    @Transactional(readOnly = true)
    public String exportCaFormat(String financialYear) {
        FinancialReportResponse income = incomeStatement(financialYear);
        FinancialReportResponse cashFlow = cashFlow(financialYear);
        FinancialReportResponse balance = balanceSheet(financialYear);

        StringBuilder builder = new StringBuilder("report,line,amount\n");
        appendReportLines(builder, income);
        appendReportLines(builder, cashFlow);
        appendReportLines(builder, balance);
        return builder.toString();
    }

    private void appendReportLines(StringBuilder builder, FinancialReportResponse report) {
        for (FinancialReportLine line : report.lines()) {
            builder.append(report.reportType()).append(',')
                    .append(escapeCsv(line.label())).append(',')
                    .append(line.amount()).append('\n');
        }
    }

    private AccountHeadEntity findAccountHead(UUID id) {
        return accountHeadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account head not found: " + id));
    }

    private FundCategoryEntity findFundCategory(UUID id) {
        return fundCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund category not found: " + id));
    }

    private LedgerEntryEntity findLedgerEntry(UUID id) {
        return ledgerEntryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger entry not found: " + id));
    }

    private ExpenseEntity findExpense(UUID id) {
        return expenseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    private VendorEntity findVendor(UUID id) {
        return vendorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + id));
    }

    private VendorPaymentEntity findVendorPayment(UUID id) {
        return vendorPaymentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor payment not found: " + id));
    }

    private BudgetEntity findBudget(UUID id) {
        return budgetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
    }

    private AccountHeadType parseAccountHeadType(String value) {
        try {
            return AccountHeadType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid account head type: " + value);
        }
    }

    private LedgerTransactionType parseLedgerTransactionType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LedgerTransactionType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid ledger transaction type: " + value);
        }
    }

    private LedgerType parseLedgerType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LedgerType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid ledger type: " + value);
        }
    }

    private ExpensePaymentStatus parseExpensePaymentStatus(String value) {
        try {
            return ExpensePaymentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid expense payment status: " + value);
        }
    }

    private VendorPaymentStatus parseVendorPaymentStatus(String value) {
        if (value == null || value.isBlank()) {
            return VendorPaymentStatus.COMPLETED;
        }
        try {
            return VendorPaymentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid vendor payment status: " + value);
        }
    }

    private LedgerType resolveLedgerType(String explicitType, AccountHeadEntity accountHead) {
        LedgerType parsed = parseLedgerType(explicitType);
        if (parsed != null) {
            return parsed;
        }
        if (accountHead != null && accountHead.getHeadType() == AccountHeadType.INCOME) {
            return LedgerType.INCOME;
        }
        return LedgerType.EXPENSE;
    }

    private String resolveCategory(String explicitCategory, AccountHeadEntity accountHead) {
        if (explicitCategory != null && !explicitCategory.isBlank()) {
            return explicitCategory.trim();
        }
        if (accountHead != null) {
            return accountHead.getHeadName();
        }
        return "GENERAL";
    }

    private String generateExpenseNumber() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String number = "EXP-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
            if (expenseRepository.findByExpenseNumberAndDeletedFalse(number).isEmpty()) {
                return number;
            }
        }
        throw new BadRequestException("Unable to generate unique expense number");
    }

    private FinancialYearRange resolveFinancialYearRange(String financialYear) {
        if (financialYear == null || financialYear.isBlank()) {
            int year = LocalDate.now().getYear();
            return new FinancialYearRange(year + "-" + (year + 1), LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year + 1, Month.DECEMBER, 31));
        }

        String normalized = financialYear.trim();
        String[] parts = normalized.split("-");
        if (parts.length != 2) {
            throw new BadRequestException("Invalid financial year format. Use YYYY-YYYY");
        }

        int startYear;
        int endYear;
        try {
            startYear = Integer.parseInt(parts[0]);
            endYear = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid financial year format. Use YYYY-YYYY");
        }
        if (endYear < startYear) {
            throw new BadRequestException("Financial year end must be >= start");
        }

        return new FinancialYearRange(normalized, LocalDate.of(startYear, Month.JANUARY, 1), LocalDate.of(endYear, Month.DECEMBER, 31));
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private AccountHeadResponse toResponse(AccountHeadEntity entity) {
        return new AccountHeadResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getHeadName(),
                entity.getHeadType(),
                entity.getParentHeadId());
    }

    private FundCategoryResponse toResponse(FundCategoryEntity entity) {
        return new FundCategoryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getCurrentBalance());
    }

    private LedgerEntryResponse toLedgerEntryResponse(LedgerEntryEntity entity) {
        return new LedgerEntryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getType(),
                entity.getCategory(),
                entity.getAccountHeadId(),
                entity.getFundCategoryId(),
                entity.getTransactionType(),
                entity.getAmount(),
                entity.getReference(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getDescription(),
                entity.getEntryDate(),
                entity.getCreatedBy());
    }

    private ExpenseResponse toResponse(ExpenseEntity entity) {
        return new ExpenseResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getExpenseNumber(),
                entity.getAccountHeadId(),
                entity.getFundCategoryId(),
                entity.getVendorId(),
                entity.getExpenseDate(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getInvoiceNumber(),
                entity.getInvoiceUrl(),
                entity.getPaymentStatus(),
                entity.getApprovedBy(),
                entity.getApprovalDate());
    }

    private VendorResponse toResponse(VendorEntity entity) {
        return new VendorResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getVendorName(),
                entity.getContactPerson(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getGstin(),
                entity.getPan(),
                entity.getVendorType(),
                entity.isActive());
    }

    private VendorPaymentResponse toResponse(VendorPaymentEntity entity) {
        return new VendorPaymentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getVendorId(),
                entity.getExpenseId(),
                entity.getPaymentDate(),
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getTransactionReference(),
                entity.getCreatedBy(),
                entity.getStatus());
    }

    private BudgetResponse toResponse(BudgetEntity entity) {
        return new BudgetResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getFinancialYear(),
                entity.getAccountHeadId(),
                entity.getBudgetedAmount());
    }

    private record FinancialYearRange(String label, LocalDate from, LocalDate to) {
    }
}
