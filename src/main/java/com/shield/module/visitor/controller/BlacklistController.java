package com.shield.module.visitor.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.visitor.dto.BlacklistCheckResponse;
import com.shield.module.visitor.dto.BlacklistCreateRequest;
import com.shield.module.visitor.dto.BlacklistResponse;
import com.shield.module.visitor.dto.BlacklistUpdateRequest;
import com.shield.module.visitor.service.VisitorService;
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
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final VisitorService visitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BlacklistResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Blacklist entries fetched", visitorService.listBlacklist(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlacklistResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Blacklist entry fetched", visitorService.getBlacklist(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<BlacklistResponse>> create(@Valid @RequestBody BlacklistCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Blacklist entry created",
                visitorService.createBlacklist(request, SecurityUtils.getCurrentPrincipal())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<BlacklistResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BlacklistUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Blacklist entry updated",
                visitorService.updateBlacklist(id, request, SecurityUtils.getCurrentPrincipal())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        visitorService.deleteBlacklist(id, SecurityUtils.getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Blacklist entry deleted", null));
    }

    @GetMapping("/check/{phone}")
    public ResponseEntity<ApiResponse<BlacklistCheckResponse>> check(@PathVariable String phone) {
        return ResponseEntity.ok(ApiResponse.ok("Blacklist check complete", visitorService.checkBlacklistByPhone(phone)));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<BlacklistResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Blacklist entry activated",
                visitorService.activateBlacklist(id, SecurityUtils.getCurrentPrincipal())));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<BlacklistResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Blacklist entry deactivated",
                visitorService.deactivateBlacklist(id, SecurityUtils.getCurrentPrincipal())));
    }
}
