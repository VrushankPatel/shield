package com.shield.module.visitor.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.visitor.dto.VisitorPassCreateRequest;
import com.shield.module.visitor.dto.VisitorPassResponse;
import com.shield.module.visitor.service.VisitorService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visitors/pass")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @PostMapping
    public ResponseEntity<ApiResponse<VisitorPassResponse>> create(@Valid @RequestBody VisitorPassCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor pass created", visitorService.createPass(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor pass fetched", visitorService.getPass(id)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor pass approved", visitorService.approve(id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<VisitorPassResponse>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Visitor pass rejected", visitorService.reject(id)));
    }
}
