package com.shield.module.visitor.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.visitor.dto.VisitorPassCreateRequest;
import com.shield.module.visitor.dto.VisitorPassPreApproveRequest;
import com.shield.module.visitor.dto.VisitorPassResponse;
import com.shield.module.visitor.dto.VisitorPassUpdateRequest;
import com.shield.module.visitor.service.VisitorService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/visitor-passes")
@RequiredArgsConstructor
public class VisitorPassController {

    private final VisitorService visitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<VisitorPassResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor passes fetched", visitorService.listPasses(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor pass fetched", visitorService.getPass(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VisitorPassResponse>> create(@Valid @RequestBody VisitorPassCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor pass created",
                visitorService.createPass(request, SecurityUtils.getCurrentPrincipal())));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> createAlias(@Valid @RequestBody VisitorPassCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor pass created",
                visitorService.createPass(request, SecurityUtils.getCurrentPrincipal())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody VisitorPassUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor pass updated",
                visitorService.updatePass(id, request, SecurityUtils.getCurrentPrincipal())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        visitorService.deletePass(id, SecurityUtils.getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Visitor pass deleted", null));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor pass cancelled",
                visitorService.cancelPass(id, SecurityUtils.getCurrentPrincipal())));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorPassResponse>>> byUnit(
            @PathVariable UUID unitId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor passes fetched", visitorService.listPassesByUnit(unitId, pageable)));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorPassResponse>>> byDate(
            @PathVariable LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor passes fetched", visitorService.listPassesByDate(date, pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorPassResponse>>> active(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Active visitor passes fetched", visitorService.listActivePasses(pageable)));
    }

    @GetMapping("/verify/{qrCode}")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> verify(@PathVariable String qrCode) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor pass verified", visitorService.verifyPassByQrCode(qrCode)));
    }

    @PostMapping("/pre-approve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> preApprove(
            @Valid @RequestBody VisitorPassPreApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor pass pre-approved",
                visitorService.preApprovePass(request, SecurityUtils.getCurrentPrincipal())));
    }
}
