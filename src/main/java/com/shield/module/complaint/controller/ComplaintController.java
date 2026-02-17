package com.shield.module.complaint.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.complaint.dto.ComplaintAssignRequest;
import com.shield.module.complaint.dto.ComplaintCreateRequest;
import com.shield.module.complaint.dto.ComplaintResponse;
import com.shield.module.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assign(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint assigned", complaintService.assign(id, request)));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint resolved", complaintService.resolve(id)));
    }
}
