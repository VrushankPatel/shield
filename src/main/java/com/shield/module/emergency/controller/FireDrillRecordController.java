package com.shield.module.emergency.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.emergency.dto.FireDrillRecordCreateRequest;
import com.shield.module.emergency.dto.FireDrillRecordResponse;
import com.shield.module.emergency.dto.FireDrillRecordUpdateRequest;
import com.shield.module.emergency.service.EmergencyService;
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
@RequestMapping("/api/v1/fire-drill-records")
@RequiredArgsConstructor
public class FireDrillRecordController {

    private final EmergencyService emergencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<FireDrillRecordResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Fire drill records fetched", emergencyService.listFireDrills(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FireDrillRecordResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Fire drill record fetched", emergencyService.getFireDrill(id)));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<PagedResponse<FireDrillRecordResponse>>> listByYear(
            @PathVariable int year,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Fire drill records by year fetched", emergencyService.listFireDrillsByYear(year, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<FireDrillRecordResponse>> create(@Valid @RequestBody FireDrillRecordCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Fire drill record created", emergencyService.createFireDrill(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<FireDrillRecordResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FireDrillRecordUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Fire drill record updated", emergencyService.updateFireDrill(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        emergencyService.deleteFireDrill(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Fire drill record deleted", null));
    }
}
