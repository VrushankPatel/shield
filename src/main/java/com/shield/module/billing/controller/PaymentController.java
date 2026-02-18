package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.billing.dto.PaymentCallbackRequest;
import com.shield.module.billing.dto.PaymentCreateRequest;
import com.shield.module.billing.dto.PaymentGatewayTransactionResponse;
import com.shield.module.billing.dto.PaymentInitiateRequest;
import com.shield.module.billing.dto.PaymentInitiateResponse;
import com.shield.module.billing.dto.PaymentResponse;
import com.shield.module.billing.dto.PaymentVerifyRequest;
import com.shield.module.billing.service.BillingService;
import com.shield.module.billing.service.PaymentGatewayService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BillingService billingService;
    private final PaymentGatewayService paymentGatewayService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PaymentResponse>> create(@Valid @RequestBody PaymentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment created", billingService.createPayment(request)));
    }

    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiate(@Valid @RequestBody PaymentInitiateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payment initiated", paymentGatewayService.initiate(request, principal)));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PaymentGatewayTransactionResponse>> verify(@Valid @RequestBody PaymentVerifyRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payment verification processed", paymentGatewayService.verify(request, principal)));
    }

    @PostMapping("/callback")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PaymentGatewayTransactionResponse>> callback(@Valid @RequestBody PaymentCallbackRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payment callback processed", paymentGatewayService.callback(request, principal)));
    }

    @PostMapping("/webhook/{provider}")
    public ResponseEntity<ApiResponse<PaymentGatewayTransactionResponse>> webhook(
            @PathVariable String provider,
            @RequestHeader(value = "X-SHIELD-SIGNATURE", required = false) String signature,
            @Valid @RequestBody PaymentCallbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Payment webhook processed",
                paymentGatewayService.callbackWebhook(provider, request, signature)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment fetched", billingService.getPayment(id)));
    }

    @GetMapping("/transaction/{transactionRef}")
    public ResponseEntity<ApiResponse<PaymentGatewayTransactionResponse>> getTransactionByRef(
            @PathVariable String transactionRef) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Payment transaction fetched",
                paymentGatewayService.getByTransactionRef(transactionRef)));
    }
}
