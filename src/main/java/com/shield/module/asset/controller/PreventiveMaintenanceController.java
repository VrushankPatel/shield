package com.shield.module.asset.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.asset.dto.PreventiveMaintenanceCreateRequest;
import com.shield.module.asset.dto.PreventiveMaintenanceResponse;
import com.shield.module.asset.dto.PreventiveMaintenanceUpdateRequest;
import com.shield.module.asset.service.PreventiveMaintenanceService;
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
@RequestMapping("/api/v1/preventive-maintenance")
@RequiredArgsConstructor
public class PreventiveMaintenanceController {

    private final PreventiveMaintenanceService preventiveMaintenanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PreventiveMaintenanceResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Preventive maintenance schedules fetched", preventiveMaintenanceService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PreventiveMaintenanceResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Preventive maintenance schedule fetched", preventiveMaintenanceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PreventiveMaintenanceResponse>> create(
            @Valid @RequestBody PreventiveMaintenanceCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Preventive maintenance schedule created", preventiveMaintenanceService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PreventiveMaintenanceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PreventiveMaintenanceUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Preventive maintenance schedule updated", preventiveMaintenanceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        preventiveMaintenanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Preventive maintenance schedule deleted", null));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<ApiResponse<PagedResponse<PreventiveMaintenanceResponse>>> listByAsset(
            @PathVariable UUID assetId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Preventive maintenance schedules by asset fetched",
                preventiveMaintenanceService.listByAsset(assetId, pageable)));
    }

    @GetMapping("/due")
    public ResponseEntity<ApiResponse<PagedResponse<PreventiveMaintenanceResponse>>> listDue(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Due preventive maintenance schedules fetched", preventiveMaintenanceService.listDue(pageable)));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PreventiveMaintenanceResponse>> execute(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Preventive maintenance schedule executed", preventiveMaintenanceService.execute(id)));
    }
}
