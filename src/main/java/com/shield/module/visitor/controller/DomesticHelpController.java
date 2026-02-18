package com.shield.module.visitor.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.visitor.dto.DomesticHelpAssignUnitRequest;
import com.shield.module.visitor.dto.DomesticHelpCreateRequest;
import com.shield.module.visitor.dto.DomesticHelpResponse;
import com.shield.module.visitor.dto.DomesticHelpUnitMappingResponse;
import com.shield.module.visitor.dto.DomesticHelpUpdateRequest;
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
@RequestMapping("/api/v1/domestic-help")
@RequiredArgsConstructor
public class DomesticHelpController {

    private final VisitorService visitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DomesticHelpResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Domestic help fetched", visitorService.listDomesticHelp(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DomesticHelpResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Domestic help fetched", visitorService.getDomesticHelp(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DomesticHelpResponse>> create(@Valid @RequestBody DomesticHelpCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Domestic help created",
                visitorService.createDomesticHelp(request, SecurityUtils.getCurrentPrincipal())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DomesticHelpResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DomesticHelpUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Domestic help updated",
                visitorService.updateDomesticHelp(id, request, SecurityUtils.getCurrentPrincipal())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        visitorService.deleteDomesticHelp(id, SecurityUtils.getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Domestic help deleted", null));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponse<DomesticHelpResponse>>> byType(
            @PathVariable String type,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Domestic help fetched", visitorService.listDomesticHelpByType(type, pageable)));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DomesticHelpResponse>> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Domestic help verified",
                visitorService.verifyDomesticHelp(id, SecurityUtils.getCurrentPrincipal())));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<PagedResponse<DomesticHelpResponse>>> byUnit(
            @PathVariable UUID unitId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Domestic help fetched", visitorService.listDomesticHelpByUnit(unitId, pageable)));
    }

    @PostMapping("/{id}/assign-unit")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DomesticHelpUnitMappingResponse>> assignUnit(
            @PathVariable UUID id,
            @Valid @RequestBody DomesticHelpAssignUnitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Domestic help assigned to unit",
                visitorService.assignDomesticHelpToUnit(id, request, SecurityUtils.getCurrentPrincipal())));
    }

    @DeleteMapping("/{helpId}/unit/{unitId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<Void>> removeUnitMapping(
            @PathVariable UUID helpId,
            @PathVariable UUID unitId) {
        visitorService.removeDomesticHelpUnitMapping(helpId, unitId, SecurityUtils.getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Domestic help mapping removed", null));
    }
}
