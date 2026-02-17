package com.shield.module.utility.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.utility.dto.ElectricityMeterCreateRequest;
import com.shield.module.utility.dto.ElectricityMeterResponse;
import com.shield.module.utility.dto.ElectricityMeterUpdateRequest;
import com.shield.module.utility.service.UtilityService;
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
@RequestMapping("/api/v1/electricity-meters")
@RequiredArgsConstructor
public class ElectricityMeterController {

    private final UtilityService utilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ElectricityMeterResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Electricity meters fetched", utilityService.listElectricityMeters(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectricityMeterResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Electricity meter fetched", utilityService.getElectricityMeter(id)));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<PagedResponse<ElectricityMeterResponse>>> listByUnit(
            @PathVariable UUID unitId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Unit electricity meters fetched", utilityService.listElectricityMetersByUnit(unitId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ElectricityMeterResponse>> create(@Valid @RequestBody ElectricityMeterCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Electricity meter created", utilityService.createElectricityMeter(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ElectricityMeterResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ElectricityMeterUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Electricity meter updated", utilityService.updateElectricityMeter(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        utilityService.deleteElectricityMeter(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Electricity meter deleted", null));
    }
}
