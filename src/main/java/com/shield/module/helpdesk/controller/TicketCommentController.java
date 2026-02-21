package com.shield.module.helpdesk.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.helpdesk.dto.HelpdeskCommentResponse;
import com.shield.module.helpdesk.dto.HelpdeskCommentUpdateRequest;
import com.shield.module.helpdesk.service.HelpdeskService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket-comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final HelpdeskService helpdeskService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskCommentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HelpdeskCommentUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk comment updated", helpdeskService.updateComment(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        helpdeskService.deleteComment(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk comment deleted", null));
    }
}
