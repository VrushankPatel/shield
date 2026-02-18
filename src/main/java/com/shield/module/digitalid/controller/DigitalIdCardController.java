package com.shield.module.digitalid.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.digitalid.dto.DigitalIdCardResponse;
import com.shield.module.digitalid.dto.DigitalIdGenerateRequest;
import com.shield.module.digitalid.dto.DigitalIdRenewRequest;
import com.shield.module.digitalid.dto.DigitalIdVerificationResponse;
import com.shield.module.digitalid.service.DigitalIdCardService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/digital-id-cards")
@RequiredArgsConstructor
public class DigitalIdCardController {

    private final DigitalIdCardService digitalIdCardService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<DigitalIdCardResponse>>> listByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Digital ID cards fetched", digitalIdCardService.listByUser(userId, pageable, principal)));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DigitalIdCardResponse>> generate(@Valid @RequestBody DigitalIdGenerateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Digital ID card generated", digitalIdCardService.generate(request, principal)));
    }

    @GetMapping("/verify/{qrCode}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DigitalIdVerificationResponse>> verifyByQrCode(@PathVariable String qrCode) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Digital ID card verification completed", digitalIdCardService.verifyByQrCode(qrCode, principal)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DigitalIdCardResponse>> getById(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Digital ID card fetched", digitalIdCardService.getById(id, principal)));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DigitalIdCardResponse>> renew(
            @PathVariable UUID id,
            @Valid @RequestBody DigitalIdRenewRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Digital ID card renewed", digitalIdCardService.renew(id, request, principal)));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DigitalIdCardResponse>> deactivate(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Digital ID card deactivated", digitalIdCardService.deactivate(id, principal)));
    }
}
