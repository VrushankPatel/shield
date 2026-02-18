package com.shield.module.kyc.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.kyc.dto.KycDecisionRequest;
import com.shield.module.kyc.dto.KycDocumentCreateRequest;
import com.shield.module.kyc.dto.KycDocumentResponse;
import com.shield.module.kyc.dto.KycDocumentUpdateRequest;
import com.shield.module.kyc.service.KycService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<KycDocumentResponse>>> listByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("KYC documents fetched", kycService.listByUser(userId, pageable, principal)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> getById(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("KYC document fetched", kycService.getById(id, principal)));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','OWNER','TENANT')")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> upload(@Valid @RequestBody KycDocumentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("KYC document uploaded", kycService.upload(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','OWNER','TENANT')")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody KycDocumentUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("KYC document updated", kycService.update(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','OWNER','TENANT')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        kycService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("KYC document deleted", null));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> verify(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("KYC document verified", kycService.verify(id, principal)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody KycDecisionRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("KYC document rejected", kycService.reject(id, request, principal)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<KycDocumentResponse>>> listPending(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Pending KYC documents fetched", kycService.listPending(pageable)));
    }
}
