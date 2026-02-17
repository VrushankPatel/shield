package com.shield.module.staff.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.staff.dto.StaffAttendanceCheckInRequest;
import com.shield.module.staff.dto.StaffAttendanceCheckOutRequest;
import com.shield.module.staff.dto.StaffAttendanceResponse;
import com.shield.module.staff.service.StaffService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff-attendance")
@RequiredArgsConstructor
public class StaffAttendanceController {

    private final StaffService staffService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffAttendanceResponse>> checkIn(
            @Valid @RequestBody StaffAttendanceCheckInRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff check-in recorded", staffService.checkIn(request, principal)));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffAttendanceResponse>> checkOut(
            @Valid @RequestBody StaffAttendanceCheckOutRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff check-out recorded", staffService.checkOut(request, principal)));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<PagedResponse<StaffAttendanceResponse>>> listByStaff(
            @PathVariable UUID staffId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Staff attendance fetched", staffService.listAttendanceByStaff(staffId, pageable)));
    }
}
