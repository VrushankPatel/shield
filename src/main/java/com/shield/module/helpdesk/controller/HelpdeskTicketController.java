package com.shield.module.helpdesk.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.helpdesk.dto.HelpdeskCommentCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskCommentResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketAssignRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketCreateRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResolveRequest;
import com.shield.module.helpdesk.dto.HelpdeskTicketResponse;
import com.shield.module.helpdesk.dto.HelpdeskTicketUpdateRequest;
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
@RequestMapping("/api/v1/helpdesk-tickets")
@RequiredArgsConstructor
public class HelpdeskTicketController {

    private final HelpdeskService helpdeskService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<HelpdeskTicketResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk tickets fetched", helpdeskService.listTickets(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskTicketResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk ticket fetched", helpdeskService.getTicket(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HelpdeskTicketResponse>> create(@Valid @RequestBody HelpdeskTicketCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk ticket created", helpdeskService.createTicket(request, principal)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskTicketResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HelpdeskTicketUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk ticket updated", helpdeskService.updateTicket(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        helpdeskService.deleteTicket(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk ticket deleted", null));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<HelpdeskTicketResponse>> assign(
            @PathVariable UUID id,
            @Valid @RequestBody HelpdeskTicketAssignRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk ticket assigned", helpdeskService.assignTicket(id, request, principal)));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<HelpdeskTicketResponse>> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody HelpdeskTicketResolveRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk ticket resolved", helpdeskService.resolveTicket(id, request, principal)));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<PagedResponse<HelpdeskCommentResponse>>> listComments(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk comments fetched", helpdeskService.listComments(id, pageable)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<HelpdeskCommentResponse>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody HelpdeskCommentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Helpdesk comment added", helpdeskService.addComment(id, request, principal)));
    }
}
