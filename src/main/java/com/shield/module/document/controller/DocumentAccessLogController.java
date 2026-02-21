package com.shield.module.document.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.document.dto.DocumentAccessLogResponse;
import com.shield.module.document.service.DocumentService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/document-access-logs")
@RequiredArgsConstructor
public class DocumentAccessLogController {

    private final DocumentService documentService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentAccessLogResponse>>> byUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Document access logs by user fetched", documentService.listAccessLogsByUser(userId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentAccessLogResponse>>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Document access logs by date range fetched",
                documentService.listAccessLogsByDateRange(from, to, pageable)));
    }
}
