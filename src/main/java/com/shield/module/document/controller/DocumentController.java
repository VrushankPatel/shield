package com.shield.module.document.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.document.dto.DocumentAccessLogResponse;
import com.shield.module.document.dto.DocumentCreateRequest;
import com.shield.module.document.dto.DocumentDownloadResponse;
import com.shield.module.document.dto.DocumentResponse;
import com.shield.module.document.dto.DocumentUpdateRequest;
import com.shield.module.document.service.DocumentService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Documents fetched", documentService.listDocuments(pageable)));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> listPublic(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Public documents fetched", documentService.listPublicDocuments(pageable)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> listByCategory(
            @PathVariable UUID categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Category documents fetched", documentService.listDocumentsByCategory(categoryId, pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> search(
            @RequestParam("q") String query,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Documents search fetched", documentService.searchDocuments(query, pageable)));
    }

    @GetMapping("/tags/{tag}")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> listByTag(
            @PathVariable String tag,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Documents by tag fetched", documentService.listDocumentsByTag(tag, pageable)));
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> listExpiring(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Expiring documents fetched", documentService.listExpiringDocuments(from, to, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getById(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Document fetched", documentService.getDocument(id, principal)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ApiResponse<DocumentDownloadResponse>> download(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Document download details fetched", documentService.downloadDocument(id, principal)));
    }

    @GetMapping("/{id}/access-logs")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentAccessLogResponse>>> accessLogsByDocument(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Document access logs fetched", documentService.listAccessLogsByDocument(id, pageable)));
    }

    @PostMapping({"", "/upload"})
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<DocumentResponse>> create(@Valid @RequestBody DocumentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Document created", documentService.createDocument(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<DocumentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Document updated", documentService.updateDocument(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        documentService.deleteDocument(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Document deleted", null));
    }
}
