package com.shield.module.utility.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.utility.dto.WaterTankCreateRequest;
import com.shield.module.utility.dto.WaterTankResponse;
import com.shield.module.utility.dto.WaterTankUpdateRequest;
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
@RequestMapping("/api/v1/water-tanks")
@RequiredArgsConstructor
public class WaterTankController {

    private final UtilityService utilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<WaterTankResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Water tanks fetched", utilityService.listWaterTanks(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WaterTankResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Water tank fetched", utilityService.getWaterTank(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WaterTankResponse>> create(@Valid @RequestBody WaterTankCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Water tank created", utilityService.createWaterTank(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WaterTankResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WaterTankUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Water tank updated", utilityService.updateWaterTank(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        utilityService.deleteWaterTank(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Water tank deleted", null));
    }
}
