package com.shield.module.parking.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.parking.dto.ParkingSlotAllocateRequest;
import com.shield.module.parking.dto.ParkingSlotCreateRequest;
import com.shield.module.parking.dto.ParkingSlotResponse;
import com.shield.module.parking.dto.ParkingSlotUpdateRequest;
import com.shield.module.parking.service.ParkingSlotService;
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
@RequestMapping("/api/v1/parking-slots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ParkingSlotResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Parking slots fetched", parkingSlotService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Parking slot fetched", parkingSlotService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> create(@Valid @RequestBody ParkingSlotCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Parking slot created", parkingSlotService.create(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ParkingSlotUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Parking slot updated", parkingSlotService.update(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        parkingSlotService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Parking slot deleted", null));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<PagedResponse<ParkingSlotResponse>>> listAvailable(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Available parking slots fetched", parkingSlotService.listAvailable(pageable)));
    }

    @PostMapping("/{id}/allocate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> allocate(
            @PathVariable UUID id,
            @Valid @RequestBody ParkingSlotAllocateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Parking slot allocated", parkingSlotService.allocate(id, request, principal)));
    }

    @PostMapping("/{id}/deallocate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> deallocate(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Parking slot deallocated", parkingSlotService.deallocate(id, principal)));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<PagedResponse<ParkingSlotResponse>>> listByUnit(
            @PathVariable UUID unitId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Parking slots by unit fetched", parkingSlotService.listByUnit(unitId, pageable)));
    }
}
