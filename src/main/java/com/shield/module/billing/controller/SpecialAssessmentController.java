package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.billing.dto.SpecialAssessmentCreateRequest;
import com.shield.module.billing.dto.SpecialAssessmentResponse;
import com.shield.module.billing.dto.SpecialAssessmentUpdateRequest;
import com.shield.module.billing.service.BillingManagementService;
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
@RequestMapping("/api/v1/special-assessments")
@RequiredArgsConstructor
public class SpecialAssessmentController {

    private final BillingManagementService billingManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SpecialAssessmentResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Special assessments fetched", billingManagementService.listSpecialAssessments(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialAssessmentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Special assessment fetched", billingManagementService.getSpecialAssessment(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SpecialAssessmentResponse>> create(@Valid @RequestBody SpecialAssessmentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Special assessment created", billingManagementService.createSpecialAssessment(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SpecialAssessmentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SpecialAssessmentUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Special assessment updated", billingManagementService.updateSpecialAssessment(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        billingManagementService.deleteSpecialAssessment(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Special assessment deleted", null));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<SpecialAssessmentResponse>>> active(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Active special assessments fetched", billingManagementService.listActiveSpecialAssessments(pageable)));
    }
}
