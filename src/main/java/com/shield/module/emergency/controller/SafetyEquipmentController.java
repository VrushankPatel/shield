package com.shield.module.emergency.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.emergency.dto.SafetyEquipmentCreateRequest;
import com.shield.module.emergency.dto.SafetyEquipmentResponse;
import com.shield.module.emergency.dto.SafetyEquipmentUpdateRequest;
import com.shield.module.emergency.service.EmergencyService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/safety-equipment")
@RequiredArgsConstructor
public class SafetyEquipmentController {

    private final EmergencyService emergencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SafetyEquipmentResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Safety equipment fetched", emergencyService.listSafetyEquipment(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SafetyEquipmentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Safety equipment fetched", emergencyService.getSafetyEquipment(id)));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponse<SafetyEquipmentResponse>>> listByType(
            @PathVariable("type") String type,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Safety equipment by type fetched", emergencyService.listSafetyEquipmentByType(type, pageable)));
    }

    @GetMapping("/inspection-due")
    public ResponseEntity<ApiResponse<PagedResponse<SafetyEquipmentResponse>>> listInspectionDue(
            @RequestParam(name = "onOrBefore", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onOrBefore,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Safety equipment inspection due fetched",
                emergencyService.listSafetyEquipmentInspectionDue(onOrBefore, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SafetyEquipmentResponse>> create(@Valid @RequestBody SafetyEquipmentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Safety equipment created", emergencyService.createSafetyEquipment(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SafetyEquipmentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SafetyEquipmentUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Safety equipment updated", emergencyService.updateSafetyEquipment(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        emergencyService.deleteSafetyEquipment(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Safety equipment deleted", null));
    }
}
