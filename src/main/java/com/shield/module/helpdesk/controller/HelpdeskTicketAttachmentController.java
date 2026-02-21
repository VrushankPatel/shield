package com.shield.module.helpdesk.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.helpdesk.service.HelpdeskService;
import com.shield.security.model.ShieldPrincipal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket-attachments")
@RequiredArgsConstructor
public class HelpdeskTicketAttachmentController {

    private final HelpdeskService helpdeskService;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        helpdeskService.deleteAttachment(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk attachment deleted", null));
    }
}
