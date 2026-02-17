package com.shield.module.utility.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.utility.dto.ElectricityReadingCreateRequest;
import com.shield.module.utility.dto.ElectricityReadingResponse;
import com.shield.module.utility.service.UtilityService;
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
@RequestMapping("/api/v1/electricity-readings")
@RequiredArgsConstructor
public class ElectricityReadingController {

    private final UtilityService utilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ElectricityReadingResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Electricity readings fetched", utilityService.listElectricityReadings(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectricityReadingResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Electricity reading fetched", utilityService.getElectricityReading(id)));
    }

    @GetMapping("/meter/{meterId}")
    public ResponseEntity<ApiResponse<PagedResponse<ElectricityReadingResponse>>> listByMeter(
            @PathVariable UUID meterId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Meter electricity readings fetched", utilityService.listElectricityReadingsByMeter(meterId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ElectricityReadingResponse>> create(@Valid @RequestBody ElectricityReadingCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Electricity reading created", utilityService.createElectricityReading(request, principal)));
    }
}
