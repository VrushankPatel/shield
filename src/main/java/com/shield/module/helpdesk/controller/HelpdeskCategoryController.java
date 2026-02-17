package com.shield.module.helpdesk.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.helpdesk.dto.HelpdeskCategoryCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCategoryResponse;
import com.shield.module.helpdesk.dto.HelpdeskCategoryUpdateRequest;
import com.shield.module.helpdesk.service.HelpdeskService;
import com.shield.security.model.ShieldPrincipal;
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
@RequestMapping("/api/v1/helpdesk-categories")
@RequiredArgsConstructor
public class HelpdeskCategoryController {

    private final HelpdeskService helpdeskService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<HelpdeskCategoryResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk categories fetched", helpdeskService.listCategories(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskCategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk category fetched", helpdeskService.getCategory(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<HelpdeskCategoryResponse>> create(@Valid @RequestBody HelpdeskCategoryCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk category created", helpdeskService.createCategory(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<HelpdeskCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HelpdeskCategoryUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk category updated", helpdeskService.updateCategory(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        helpdeskService.deleteCategory(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk category deleted", null));
    }
}
