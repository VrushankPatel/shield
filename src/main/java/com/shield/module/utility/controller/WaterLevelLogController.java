package com.shield.module.utility.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.utility.dto.WaterLevelLogCreateRequest;
import com.shield.module.utility.dto.WaterLevelChartDataResponse;
import com.shield.module.utility.dto.WaterLevelLogResponse;
import com.shield.module.utility.service.UtilityService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/water-level-logs")
@RequiredArgsConstructor
public class WaterLevelLogController {

    private final UtilityService utilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<WaterLevelLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Water level logs fetched", utilityService.listWaterLevelLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WaterLevelLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Water level log fetched", utilityService.getWaterLevelLog(id)));
    }

    @GetMapping("/tank/{tankId}")
    public ResponseEntity<ApiResponse<PagedResponse<WaterLevelLogResponse>>> listByTank(
            @PathVariable UUID tankId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Tank water level logs fetched", utilityService.listWaterLevelLogsByTank(tankId, pageable)));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<WaterLevelLogResponse>> current(
            @RequestParam(required = false) UUID tankId) {
        return ResponseEntity.ok(ApiResponse.ok("Current water level log fetched", utilityService.getCurrentWaterLevelLog(tankId)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<WaterLevelLogResponse>>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Water level logs by date range fetched",
                utilityService.listWaterLevelLogsByDateRange(from, to, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<WaterLevelLogResponse>> create(@Valid @RequestBody WaterLevelLogCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Water level log created", utilityService.createWaterLevelLog(request, principal)));
    }

    @GetMapping("/chart-data")
    public ResponseEntity<ApiResponse<WaterLevelChartDataResponse>> chartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID tankId,
            @RequestParam(required = false) Integer maxPoints) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Water level chart data fetched",
                utilityService.getWaterLevelChartData(from, to, tankId, maxPoints)));
    }
}
