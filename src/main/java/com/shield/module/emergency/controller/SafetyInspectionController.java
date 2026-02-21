package com.shield.module.emergency.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.emergency.dto.SafetyInspectionCreateRequest;
import com.shield.module.emergency.dto.SafetyInspectionResponse;
import com.shield.module.emergency.dto.SafetyInspectionUpdateRequest;
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
@RequestMapping("/api/v1/safety-inspections")
@RequiredArgsConstructor
public class SafetyInspectionController {

    private final EmergencyService emergencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SafetyInspectionResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Safety inspections fetched", emergencyService.listSafetyInspections(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SafetyInspectionResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Safety inspection fetched", emergencyService.getSafetyInspection(id)));
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<ApiResponse<PagedResponse<SafetyInspectionResponse>>> listByEquipment(
            @PathVariable UUID equipmentId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Safety inspections by equipment fetched",
                emergencyService.listSafetyInspectionsByEquipment(equipmentId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<SafetyInspectionResponse>>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Safety inspections by date range fetched",
                emergencyService.listSafetyInspectionsByDateRange(from, to, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SafetyInspectionResponse>> create(@Valid @RequestBody SafetyInspectionCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Safety inspection created", emergencyService.createSafetyInspection(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SafetyInspectionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SafetyInspectionUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Safety inspection updated", emergencyService.updateSafetyInspection(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        emergencyService.deleteSafetyInspection(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Safety inspection deleted", null));
    }
}
