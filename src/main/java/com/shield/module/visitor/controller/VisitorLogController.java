package com.shield.module.visitor.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.visitor.dto.VisitorLogEntryRequest;
import com.shield.module.visitor.dto.VisitorLogExitRequest;
import com.shield.module.visitor.dto.VisitorLogResponse;
import com.shield.module.visitor.service.VisitorService;
import jakarta.validation.Valid;
import java.time.Instant;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visitor-logs")
@RequiredArgsConstructor
public class VisitorLogController {

    private final VisitorService visitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<VisitorLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor logs fetched", visitorService.listVisitorLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitorLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor log fetched", visitorService.getVisitorLog(id)));
    }

    @PostMapping("/entry")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<VisitorLogResponse>> entry(@Valid @RequestBody VisitorLogEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor entry logged",
                visitorService.logEntry(request, SecurityUtils.getCurrentPrincipal())));
    }

    @PostMapping("/exit")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<VisitorLogResponse>> exit(@Valid @RequestBody VisitorLogExitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor exit logged",
                visitorService.logExit(request, SecurityUtils.getCurrentPrincipal())));
    }

    @GetMapping("/pass/{passId}")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorLogResponse>>> byPass(
            @PathVariable UUID passId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor logs fetched", visitorService.listVisitorLogsByPass(passId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorLogResponse>>> byDateRange(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor logs fetched", visitorService.listVisitorLogsByDateRange(from, to, pageable)));
    }

    @GetMapping("/currently-inside")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorLogResponse>>> currentlyInside(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Visitors currently inside fetched", visitorService.listCurrentlyInside(pageable)));
    }
}
