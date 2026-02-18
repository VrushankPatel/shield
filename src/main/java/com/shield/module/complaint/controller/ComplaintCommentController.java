package com.shield.module.complaint.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.complaint.dto.ComplaintCommentResponse;
import com.shield.module.complaint.dto.ComplaintCommentUpdateRequest;
import com.shield.module.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class ComplaintCommentController {

    private final ComplaintService complaintService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','OWNER','TENANT','SECURITY')")
    public ResponseEntity<ApiResponse<ComplaintCommentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintCommentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Complaint comment updated", complaintService.updateComment(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','OWNER','TENANT','SECURITY')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        complaintService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.ok("Complaint comment deleted", null));
    }
}
