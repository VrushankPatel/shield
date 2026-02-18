package com.shield.module.newsletter.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.newsletter.dto.NewsletterCreateRequest;
import com.shield.module.newsletter.dto.NewsletterResponse;
import com.shield.module.newsletter.dto.NewsletterUpdateRequest;
import com.shield.module.newsletter.service.NewsletterService;
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
@RequestMapping("/api/v1/newsletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NewsletterResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Newsletters fetched", newsletterService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsletterResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Newsletter fetched", newsletterService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<NewsletterResponse>> create(@Valid @RequestBody NewsletterCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Newsletter created", newsletterService.create(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<NewsletterResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody NewsletterUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity
                .ok(ApiResponse.ok("Newsletter updated", newsletterService.update(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        newsletterService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Newsletter deleted", null));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<NewsletterResponse>> publish(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Newsletter published", newsletterService.publish(id, principal)));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<PagedResponse<NewsletterResponse>>> listByYear(@PathVariable int year,
            Pageable pageable) {
        return ResponseEntity
                .ok(ApiResponse.ok("Newsletters fetched by year", newsletterService.listByYear(year, pageable)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ApiResponse<NewsletterResponse>> download(@PathVariable UUID id) {
        return ResponseEntity
                .ok(ApiResponse.ok("Newsletter download info fetched", newsletterService.getDownloadInfo(id)));
    }
}
