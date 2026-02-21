package com.shield.module.visitor.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.visitor.dto.DeliveryLogCreateRequest;
import com.shield.module.visitor.dto.DeliveryLogResponse;
import com.shield.module.visitor.service.VisitorService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delivery-logs")
@RequiredArgsConstructor
public class DeliveryLogController {

    private static final String DELIVERY_LOGS_FETCHED = "Delivery logs fetched";

    private final VisitorService visitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DeliveryLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(DELIVERY_LOGS_FETCHED, visitorService.listDeliveryLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Delivery log fetched", visitorService.getDeliveryLog(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<DeliveryLogResponse>> create(@Valid @RequestBody DeliveryLogCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Delivery log created",
                visitorService.createDeliveryLog(request, SecurityUtils.getCurrentPrincipal())));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<PagedResponse<DeliveryLogResponse>>> byUnit(
            @PathVariable UUID unitId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(DELIVERY_LOGS_FETCHED, visitorService.listDeliveryLogsByUnit(unitId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<DeliveryLogResponse>>> byDateRange(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(DELIVERY_LOGS_FETCHED, visitorService.listDeliveryLogsByDateRange(from, to, pageable)));
    }

    @GetMapping("/partner/{partner}")
    public ResponseEntity<ApiResponse<PagedResponse<DeliveryLogResponse>>> byPartner(
            @PathVariable String partner,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(DELIVERY_LOGS_FETCHED, visitorService.listDeliveryLogsByPartner(partner, pageable)));
    }
}
