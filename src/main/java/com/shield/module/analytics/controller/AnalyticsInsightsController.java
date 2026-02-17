package com.shield.module.analytics.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.analytics.dto.AmenityUtilizationResponse;
import com.shield.module.analytics.dto.AssetFailureFrequencyResponse;
import com.shield.module.analytics.dto.CollectionEfficiencyResponse;
import com.shield.module.analytics.dto.ComplaintResolutionTimeResponse;
import com.shield.module.analytics.dto.DefaulterTrendResponse;
import com.shield.module.analytics.dto.ExpenseDistributionResponse;
import com.shield.module.analytics.dto.FundAllocationResponse;
import com.shield.module.analytics.dto.OccupancyRateResponse;
import com.shield.module.analytics.dto.StaffAttendanceSummaryResponse;
import com.shield.module.analytics.dto.VisitorTrendResponse;
import com.shield.module.analytics.service.AnalyticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsInsightsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/collection-efficiency")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<CollectionEfficiencyResponse>> collectionEfficiency() {
        return ResponseEntity.ok(ApiResponse.ok("Collection efficiency fetched", analyticsService.getCollectionEfficiency()));
    }

    @GetMapping("/expense-distribution")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<ExpenseDistributionResponse>>> expenseDistribution() {
        return ResponseEntity.ok(ApiResponse.ok("Expense distribution fetched", analyticsService.getExpenseDistribution()));
    }

    @GetMapping("/complaint-resolution-time")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ComplaintResolutionTimeResponse>> complaintResolutionTime() {
        return ResponseEntity.ok(ApiResponse.ok("Complaint resolution time fetched", analyticsService.getComplaintResolutionTime()));
    }

    @GetMapping("/asset-failure-frequency")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<AssetFailureFrequencyResponse>>> assetFailureFrequency() {
        return ResponseEntity.ok(ApiResponse.ok("Asset failure frequency fetched", analyticsService.getAssetFailureFrequency()));
    }

    @GetMapping("/occupancy-rate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<OccupancyRateResponse>> occupancyRate() {
        return ResponseEntity.ok(ApiResponse.ok("Occupancy rate fetched", analyticsService.getOccupancyRate()));
    }

    @GetMapping("/amenity-utilization")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<AmenityUtilizationResponse>>> amenityUtilization() {
        return ResponseEntity.ok(ApiResponse.ok("Amenity utilization fetched", analyticsService.getAmenityUtilization()));
    }

    @GetMapping("/defaulter-trend")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<DefaulterTrendResponse>>> defaulterTrend() {
        return ResponseEntity.ok(ApiResponse.ok("Defaulter trend fetched", analyticsService.getDefaulterTrend()));
    }

    @GetMapping("/fund-allocation")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<FundAllocationResponse>>> fundAllocation() {
        return ResponseEntity.ok(ApiResponse.ok("Fund allocation fetched", analyticsService.getFundAllocation()));
    }

    @GetMapping("/staff-attendance-summary")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffAttendanceSummaryResponse>> staffAttendanceSummary() {
        return ResponseEntity.ok(ApiResponse.ok("Staff attendance summary fetched", analyticsService.getStaffAttendanceSummary()));
    }

    @GetMapping("/visitor-trends")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<VisitorTrendResponse>>> visitorTrends() {
        return ResponseEntity.ok(ApiResponse.ok("Visitor trends fetched", analyticsService.getVisitorTrends()));
    }
}
