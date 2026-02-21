package com.shield.module.utility.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.utility.dto.GeneratorLogCreateRequest;
import com.shield.module.utility.dto.GeneratorLogResponse;
import com.shield.module.utility.dto.GeneratorLogSummaryResponse;
import com.shield.module.utility.dto.GeneratorLogUpdateRequest;
import com.shield.module.utility.service.UtilityService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/generator-logs")
@RequiredArgsConstructor
public class GeneratorLogController {

    private final UtilityService utilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<GeneratorLogResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Generator logs fetched", utilityService.listGeneratorLogs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneratorLogResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Generator log fetched", utilityService.getGeneratorLog(id)));
    }

    @GetMapping("/generator/{generatorId}")
    public ResponseEntity<ApiResponse<PagedResponse<GeneratorLogResponse>>> listByGenerator(
            @PathVariable UUID generatorId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Generator logs by generator fetched",
                utilityService.listGeneratorLogsByGenerator(generatorId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<GeneratorLogResponse>>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID generatorId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Generator logs by date range fetched",
                utilityService.listGeneratorLogsByDateRange(from, to, generatorId, pageable)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<GeneratorLogSummaryResponse>> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID generatorId) {
        return ResponseEntity.ok(ApiResponse.ok("Generator logs summary fetched",
                utilityService.getGeneratorLogSummary(from, to, generatorId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<GeneratorLogResponse>> create(@Valid @RequestBody GeneratorLogCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Generator log created", utilityService.createGeneratorLog(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<GeneratorLogResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody GeneratorLogUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Generator log updated", utilityService.updateGeneratorLog(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        utilityService.deleteGeneratorLog(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Generator log deleted", null));
    }
}
