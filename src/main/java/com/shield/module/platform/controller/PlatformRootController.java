package com.shield.module.platform.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.platform.dto.RootAuthResponse;
import com.shield.module.platform.dto.RootChangePasswordRequest;
import com.shield.module.platform.dto.RootLoginRequest;
import com.shield.module.platform.dto.RootRefreshRequest;
import com.shield.module.platform.dto.SocietyOnboardingRequest;
import com.shield.module.platform.dto.SocietyOnboardingResponse;
import com.shield.module.platform.service.PlatformRootService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class PlatformRootController {

    private final PlatformRootService platformRootService;

    @PostMapping("/root/login")
    public ResponseEntity<ApiResponse<RootAuthResponse>> rootLogin(@Valid @RequestBody RootLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Root login successful", platformRootService.login(request)));
    }

    @PostMapping("/root/refresh")
    public ResponseEntity<ApiResponse<RootAuthResponse>> rootRefresh(@Valid @RequestBody RootRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Root token refreshed", platformRootService.refresh(request)));
    }

    @PostMapping("/root/change-password")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse<Void>> rootChangePassword(@Valid @RequestBody RootChangePasswordRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        platformRootService.changePassword(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Root password changed successfully. Login again with new password.", null));
    }

    @PostMapping("/societies")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse<SocietyOnboardingResponse>> createSociety(@Valid @RequestBody SocietyOnboardingRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        SocietyOnboardingResponse response = platformRootService.createSocietyWithAdmin(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Society and admin created", response));
    }
}
