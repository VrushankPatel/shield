package com.shield.module.amenities.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.amenities.dto.AmenityBookingCreateRequest;
import com.shield.module.amenities.dto.AmenityBookingResponse;
import com.shield.module.amenities.dto.AmenityCreateRequest;
import com.shield.module.amenities.dto.AmenityResponse;
import com.shield.module.amenities.service.AmenitiesService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
public class AmenitiesController {

    private final AmenitiesService amenitiesService;

    @PostMapping
    public ResponseEntity<ApiResponse<AmenityResponse>> create(@Valid @RequestBody AmenityCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity created", amenitiesService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AmenityResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Amenities fetched", amenitiesService.list(pageable)));
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> book(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityBookingCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booked", amenitiesService.book(id, request)));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityBookingResponse>>> listBookings(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity bookings fetched", amenitiesService.listBookings(pageable)));
    }
}
