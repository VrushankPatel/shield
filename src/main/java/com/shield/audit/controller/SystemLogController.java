package com.shield.audit.controller;

import com.shield.audit.dto.SystemLogResponse;
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
@RequestMapping("/api/v1/system-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
public class SystemLogController {

    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv");

    private final AuditQueryService auditQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SystemLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("System logs fetched", auditQueryService.listSystemLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("System log fetched", auditQueryService.getSystemLog(id)));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<PagedResponse<SystemLogResponse>>> listByLevel(
            @PathVariable String level,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("System logs by level fetched", auditQueryService.listSystemLogsByLevel(level, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<SystemLogResponse>>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "System logs by date range fetched",
                auditQueryService.listSystemLogsByDateRange(from, to, pageable)));
    }

    @GetMapping("/export")
    public ResponseEntity<String> export() {
        return ResponseEntity.ok()
                .contentType(CSV_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=system-logs.csv")
                .body(auditQueryService.exportSystemLogsCsv());
    }
}
