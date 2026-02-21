package com.shield.module.utility.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.utility.dto.DieselGeneratorCreateRequest;
import com.shield.module.utility.dto.DieselGeneratorResponse;
import com.shield.module.utility.dto.DieselGeneratorUpdateRequest;
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
@RequestMapping("/api/v1/diesel-generators")
@RequiredArgsConstructor
public class DieselGeneratorController {

    private final UtilityService utilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DieselGeneratorResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Diesel generators fetched", utilityService.listDieselGenerators(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DieselGeneratorResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Diesel generator fetched", utilityService.getDieselGenerator(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<DieselGeneratorResponse>> create(@Valid @RequestBody DieselGeneratorCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Diesel generator created", utilityService.createDieselGenerator(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<DieselGeneratorResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DieselGeneratorUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Diesel generator updated", utilityService.updateDieselGenerator(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        utilityService.deleteDieselGenerator(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Diesel generator deleted", null));
    }
}
