package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.billing.dto.PaymentCreateRequest;
import com.shield.module.billing.dto.PaymentResponse;
import com.shield.module.billing.service.BillingService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(@Valid @RequestBody PaymentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment created", billingService.createPayment(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment fetched", billingService.getPayment(id)));
    }
}
