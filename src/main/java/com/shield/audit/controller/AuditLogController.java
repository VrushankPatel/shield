package com.shield.audit.controller;

import com.shield.audit.dto.AuditLogResponse;
import com.shield.audit.service.AuditQueryService;
import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
public class AuditLogController {

    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv");

    private final AuditQueryService auditQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs fetched", auditQueryService.listAuditLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Audit log fetched", auditQueryService.getAuditLog(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> listByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs by user fetched", auditQueryService.listAuditLogsByUser(userId, pageable)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> listByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Audit logs by entity fetched",
                auditQueryService.listAuditLogsByEntity(entityType, entityId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Audit logs by date range fetched",
                auditQueryService.listAuditLogsByDateRange(from, to, pageable)));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> listByAction(
            @PathVariable String action,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Audit logs by action fetched",
                auditQueryService.listAuditLogsByAction(action, pageable)));
    }

    @GetMapping("/export")
    public ResponseEntity<String> export() {
        return ResponseEntity.ok()
                .contentType(CSV_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .body(auditQueryService.exportAuditLogsCsv());
    }
}
