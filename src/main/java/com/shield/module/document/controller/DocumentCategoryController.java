package com.shield.module.document.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.document.dto.DocumentCategoryCreateRequest;
import com.shield.module.document.dto.DocumentCategoryResponse;
import com.shield.module.document.dto.DocumentCategoryTreeResponse;
import com.shield.module.document.dto.DocumentCategoryUpdateRequest;
import com.shield.module.document.service.DocumentService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/document-categories")
@RequiredArgsConstructor
public class DocumentCategoryController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DocumentCategoryResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Document categories fetched", documentService.listCategories(pageable)));
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResponse<List<DocumentCategoryTreeResponse>>> hierarchy() {
        return ResponseEntity.ok(ApiResponse.ok("Document category hierarchy fetched", documentService.listCategoryHierarchy()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Document category fetched", documentService.getCategory(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> create(@Valid @RequestBody DocumentCategoryCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Document category created", documentService.createCategory(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentCategoryUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Document category updated", documentService.updateCategory(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        documentService.deleteCategory(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Document category deleted", null));
    }
}
