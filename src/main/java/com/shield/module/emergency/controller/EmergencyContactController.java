package com.shield.module.emergency.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.emergency.dto.EmergencyContactCreateRequest;
import com.shield.module.emergency.dto.EmergencyContactResponse;
import com.shield.module.emergency.dto.EmergencyContactUpdateRequest;
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
@RequestMapping("/api/v1/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyService emergencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<EmergencyContactResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Emergency contacts fetched", emergencyService.listContacts(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Emergency contact fetched", emergencyService.getContact(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> create(@Valid @RequestBody EmergencyContactCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Emergency contact created", emergencyService.createContact(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmergencyContactUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Emergency contact updated", emergencyService.updateContact(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        emergencyService.deleteContact(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Emergency contact deleted", null));
    }
}
