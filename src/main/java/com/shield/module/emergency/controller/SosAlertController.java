package com.shield.module.emergency.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.emergency.dto.SosAlertRaiseRequest;
import com.shield.module.emergency.dto.SosAlertResolveRequest;
import com.shield.module.emergency.dto.SosAlertResponse;
import com.shield.module.emergency.dto.SosAlertResponseRequest;
import com.shield.module.emergency.service.EmergencyService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sos-alerts")
@RequiredArgsConstructor
public class SosAlertController {

    private final EmergencyService emergencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SosAlertResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("SOS alerts fetched", emergencyService.listAlerts(pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<SosAlertResponse>>> listActive(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Active SOS alerts fetched", emergencyService.listActiveAlerts(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SosAlertResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("SOS alert fetched", emergencyService.getAlert(id)));
    }

    @PostMapping("/raise")
    public ResponseEntity<ApiResponse<SosAlertResponse>> raise(@Valid @RequestBody SosAlertRaiseRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("SOS alert raised", emergencyService.raiseAlert(request, principal)));
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<SosAlertResponse>> respond(
            @PathVariable UUID id,
            @Valid @RequestBody SosAlertResponseRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("SOS alert responded", emergencyService.respondAlert(id, request, principal)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<SosAlertResponse>> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody SosAlertResolveRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("SOS alert resolved", emergencyService.resolveAlert(id, request, principal)));
    }
}
