package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.billing.dto.BillGenerateRequest;
import com.shield.module.billing.dto.BillResponse;
import com.shield.module.billing.service.BillingService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<BillResponse>> generate(@Valid @RequestBody BillGenerateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Bill generated", billingService.generate(request)));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getByUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(ApiResponse.ok("Bills fetched", billingService.getByUnit(unitId)));
    }
}
