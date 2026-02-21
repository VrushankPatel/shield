package com.shield.module.complaint.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.complaint.dto.ComplaintAssignRequest;
import com.shield.module.complaint.dto.ComplaintCommentCreateRequest;
import com.shield.module.complaint.dto.ComplaintCommentResponse;
import com.shield.module.complaint.dto.ComplaintCreateRequest;
import com.shield.module.complaint.dto.ComplaintResolveRequest;
import com.shield.module.complaint.dto.ComplaintResponse;
import com.shield.module.complaint.dto.ComplaintStatisticsResponse;
import com.shield.module.complaint.dto.ComplaintUpdateRequest;
import com.shield.module.complaint.entity.ComplaintPriority;
import com.shield.module.complaint.entity.ComplaintStatus;
import com.shield.module.complaint.service.ComplaintService;
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
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResponse>> create(@Valid @RequestBody ComplaintCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint created", complaintService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Complaints fetched", complaintService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint fetched", complaintService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint updated", complaintService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        complaintService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Complaint deleted", null));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignPut(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint assigned", complaintService.assign(id, request)));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignPost(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint assigned", complaintService.assign(id, request)));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolvePut(
            @PathVariable UUID id,
            @RequestBody(required = false) ComplaintResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint resolved", complaintService.resolve(id, request)));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolvePost(
            @PathVariable UUID id,
            @RequestBody(required = false) ComplaintResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint resolved", complaintService.resolve(id, request)));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint closed", complaintService.close(id)));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> reopen(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint reopened", complaintService.reopen(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> listByStatus(
            @PathVariable ComplaintStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Complaints by status fetched", complaintService.listByStatus(status, pageable)));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> listByPriority(
            @PathVariable ComplaintPriority priority,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Complaints by priority fetched", complaintService.listByPriority(priority, pageable)));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> listByAsset(
            @PathVariable UUID assetId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Complaints by asset fetched", complaintService.listByAsset(assetId, pageable)));
    }

    @GetMapping("/my-complaints")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> myComplaints(Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("My complaints fetched", complaintService.listMyComplaints(principal.userId(), pageable)));
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> assignedToMe(Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Assigned complaints fetched", complaintService.listAssignedToMe(principal.userId(), pageable)));
    }

    @GetMapping("/sla-breached")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> slaBreached(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("SLA breached complaints fetched", complaintService.listSlaBreached(pageable)));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ComplaintStatisticsResponse>> statistics() {
        return ResponseEntity.ok(ApiResponse.ok("Complaint statistics fetched", complaintService.statistics()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintCommentResponse>>> listComments(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint comments fetched", complaintService.listComments(id, pageable)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<ComplaintCommentResponse>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintCommentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint comment added", complaintService.addComment(id, request)));
    }
}
