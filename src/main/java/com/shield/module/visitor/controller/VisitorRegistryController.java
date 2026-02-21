package com.shield.module.visitor.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.visitor.dto.VisitorCreateRequest;
import com.shield.module.visitor.dto.VisitorResponse;
import com.shield.module.visitor.dto.VisitorUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visitors")
@RequiredArgsConstructor
public class VisitorRegistryController {

    private static final String VISITORS_FETCHED = "Visitors fetched";

    private final VisitorService visitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<VisitorResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(VISITORS_FETCHED, visitorService.listVisitors(pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorResponse>>> search(
            @RequestParam(name = "q", required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(VISITORS_FETCHED, visitorService.searchVisitors(query, pageable)));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<PagedResponse<VisitorResponse>>> byPhone(
            @PathVariable String phone,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(VISITORS_FETCHED, visitorService.listVisitorsByPhone(phone, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitorResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor fetched", visitorService.getVisitor(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<VisitorResponse>> create(@Valid @RequestBody VisitorCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor created",
                visitorService.createVisitor(request, SecurityUtils.getCurrentPrincipal())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<VisitorResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody VisitorUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Visitor updated",
                visitorService.updateVisitor(id, request, SecurityUtils.getCurrentPrincipal())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        visitorService.deleteVisitor(id, SecurityUtils.getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Visitor deleted", null));
    }
}
