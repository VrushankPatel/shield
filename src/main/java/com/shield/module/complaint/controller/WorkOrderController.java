package com.shield.module.complaint.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.complaint.dto.WorkOrderCreateRequest;
import com.shield.module.complaint.dto.WorkOrderResponse;
import com.shield.module.complaint.dto.WorkOrderUpdateRequest;
import com.shield.module.complaint.entity.WorkOrderStatus;
import com.shield.module.complaint.service.WorkOrderService;
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
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<WorkOrderResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Work orders fetched", workOrderService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Work order fetched", workOrderService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Work order created", workOrderService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkOrderUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Work order updated", workOrderService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        workOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Work order deleted", null));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> start(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Work order started", workOrderService.start(id)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Work order completed", workOrderService.complete(id)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Work order cancelled", workOrderService.cancel(id)));
    }

    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<ApiResponse<PagedResponse<WorkOrderResponse>>> listByComplaint(
            @PathVariable UUID complaintId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Work orders by complaint fetched", workOrderService.listByComplaint(complaintId, pageable)));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<PagedResponse<WorkOrderResponse>>> listByVendor(
            @PathVariable UUID vendorId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Work orders by vendor fetched", workOrderService.listByVendor(vendorId, pageable)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<WorkOrderResponse>>> listByStatus(
            @PathVariable WorkOrderStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Work orders by status fetched", workOrderService.listByStatus(status, pageable)));
    }
}
