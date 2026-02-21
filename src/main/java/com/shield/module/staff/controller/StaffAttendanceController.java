package com.shield.module.staff.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.staff.dto.StaffAttendanceCheckInRequest;
import com.shield.module.staff.dto.StaffAttendanceCheckOutRequest;
import com.shield.module.staff.dto.StaffAttendanceResponse;
import com.shield.module.staff.dto.StaffAttendanceSummaryResponse;
import com.shield.module.staff.dto.StaffAttendanceUpdateRequest;
import com.shield.module.staff.service.StaffService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff-attendance")
@RequiredArgsConstructor
public class StaffAttendanceController {

    private static final String STAFF_ATTENDANCE_FETCHED = "Staff attendance fetched";

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

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StaffAttendanceResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(STAFF_ATTENDANCE_FETCHED, staffService.listAttendance(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffAttendanceResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(STAFF_ATTENDANCE_FETCHED, staffService.getAttendanceById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffAttendanceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StaffAttendanceUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff attendance updated", staffService.updateAttendance(id, request, principal)));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<PagedResponse<StaffAttendanceResponse>>> listByStaff(
            @PathVariable UUID staffId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(STAFF_ATTENDANCE_FETCHED, staffService.listAttendanceByStaff(staffId, pageable)));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<PagedResponse<StaffAttendanceResponse>>> listByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Staff attendance by date fetched", staffService.listAttendanceByDate(date, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<StaffAttendanceResponse>>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Staff attendance by date range fetched",
                staffService.listAttendanceByDateRange(from, to, pageable)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StaffAttendanceSummaryResponse>> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok("Staff attendance summary fetched", staffService.summarizeAttendance(from, to)));
    }
}
