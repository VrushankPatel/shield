package com.shield.audit.controller;

import com.shield.audit.dto.ApiRequestLogResponse;
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
@RequestMapping("/api/v1/api-request-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
public class ApiRequestLogController {

    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv");

    private final AuditQueryService auditQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ApiRequestLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("API request logs fetched", auditQueryService.listApiRequestLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiRequestLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("API request log fetched", auditQueryService.getApiRequestLog(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<ApiRequestLogResponse>>> listByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "API request logs by user fetched",
                auditQueryService.listApiRequestLogsByUser(userId, pageable)));
    }

    @GetMapping("/endpoint/{endpoint}")
    public ResponseEntity<ApiResponse<PagedResponse<ApiRequestLogResponse>>> listByEndpoint(
            @PathVariable String endpoint,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "API request logs by endpoint fetched",
                auditQueryService.listApiRequestLogsByEndpoint(endpoint, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<ApiRequestLogResponse>>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "API request logs by date range fetched",
                auditQueryService.listApiRequestLogsByDateRange(from, to, pageable)));
    }

    @GetMapping("/slow-requests")
    public ResponseEntity<ApiResponse<PagedResponse<ApiRequestLogResponse>>> listSlowRequests(
            @RequestParam(defaultValue = "1000") long thresholdMs,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Slow API requests fetched",
                auditQueryService.listSlowApiRequests(thresholdMs, pageable)));
    }

    @GetMapping("/failed-requests")
    public ResponseEntity<ApiResponse<PagedResponse<ApiRequestLogResponse>>> listFailedRequests(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Failed API requests fetched",
                auditQueryService.listFailedApiRequests(pageable)));
    }

    @GetMapping("/export")
    public ResponseEntity<String> export() {
        return ResponseEntity.ok()
                .contentType(CSV_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=api-request-logs.csv")
                .body(auditQueryService.exportApiRequestLogsCsv());
    }
}
