package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.billing.dto.PaymentReminderResponse;
import com.shield.module.billing.dto.PaymentReminderScheduleRequest;
import com.shield.module.billing.dto.PaymentReminderSendRequest;
import com.shield.module.billing.service.BillingManagementService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-reminders")
@RequiredArgsConstructor
public class PaymentReminderController {

    private final BillingManagementService billingManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PaymentReminderResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Payment reminders fetched", billingManagementService.listPaymentReminders(pageable)));
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PaymentReminderResponse>> send(@Valid @RequestBody PaymentReminderSendRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payment reminder sent", billingManagementService.sendPaymentReminder(request, principal)));
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PaymentReminderResponse>> schedule(@Valid @RequestBody PaymentReminderScheduleRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payment reminder scheduled", billingManagementService.schedulePaymentReminder(request, principal)));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentReminderResponse>>> byInvoice(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(ApiResponse.ok("Payment reminders by invoice fetched", billingManagementService.listPaymentRemindersByInvoice(invoiceId)));
    }
}
