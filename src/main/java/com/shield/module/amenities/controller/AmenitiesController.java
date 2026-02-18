package com.shield.module.amenities.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.amenities.dto.AmenityAvailabilityResponse;
import com.shield.module.amenities.dto.AmenityBookingCreateRequest;
import com.shield.module.amenities.dto.AmenityBookingResponse;
import com.shield.module.amenities.dto.AmenityBookingRuleCreateRequest;
import com.shield.module.amenities.dto.AmenityBookingRuleResponse;
import com.shield.module.amenities.dto.AmenityBookingRuleUpdateRequest;
import com.shield.module.amenities.dto.AmenityBookingUpdateRequest;
import com.shield.module.amenities.dto.AmenityCancellationPolicyCreateRequest;
import com.shield.module.amenities.dto.AmenityCancellationPolicyResponse;
import com.shield.module.amenities.dto.AmenityCancellationPolicyUpdateRequest;
import com.shield.module.amenities.dto.AmenityCreateRequest;
import com.shield.module.amenities.dto.AmenityPricingCreateRequest;
import com.shield.module.amenities.dto.AmenityPricingResponse;
import com.shield.module.amenities.dto.AmenityPricingUpdateRequest;
import com.shield.module.amenities.dto.AmenityResponse;
import com.shield.module.amenities.dto.AmenityTimeSlotCreateRequest;
import com.shield.module.amenities.dto.AmenityTimeSlotResponse;
import com.shield.module.amenities.dto.AmenityTimeSlotUpdateRequest;
import com.shield.module.amenities.dto.AmenityUpdateRequest;
import com.shield.module.amenities.service.AmenitiesService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AmenitiesController {

    private final AmenitiesService amenitiesService;

    @PostMapping("/amenities")
    public ResponseEntity<ApiResponse<AmenityResponse>> create(@Valid @RequestBody AmenityCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity created", amenitiesService.create(request)));
    }

    @GetMapping("/amenities")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Amenities fetched", amenitiesService.list(pageable)));
    }

    @GetMapping("/amenities/{id}")
    public ResponseEntity<ApiResponse<AmenityResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity fetched", amenitiesService.get(id)));
    }

    @PutMapping("/amenities/{id}")
    public ResponseEntity<ApiResponse<AmenityResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity updated", amenitiesService.update(id, request)));
    }

    @DeleteMapping("/amenities/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        amenitiesService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Amenity deleted", null));
    }

    @GetMapping("/amenities/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityResponse>>> listByType(
            @PathVariable String type,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Amenities by type fetched", amenitiesService.listByType(type, pageable)));
    }

    @GetMapping("/amenities/available")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityResponse>>> listAvailable(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Available amenities fetched", amenitiesService.listAvailable(pageable)));
    }

    @PostMapping("/amenities/{id}/activate")
    public ResponseEntity<ApiResponse<AmenityResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity activated", amenitiesService.activate(id)));
    }

    @PostMapping("/amenities/{id}/deactivate")
    public ResponseEntity<ApiResponse<AmenityResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity deactivated", amenitiesService.deactivate(id)));
    }

    @GetMapping("/amenities/{id}/time-slots")
    public ResponseEntity<ApiResponse<List<AmenityTimeSlotResponse>>> listTimeSlots(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity time slots fetched", amenitiesService.listTimeSlots(id)));
    }

    @PostMapping("/amenities/{id}/time-slots")
    public ResponseEntity<ApiResponse<AmenityTimeSlotResponse>> createTimeSlot(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityTimeSlotCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity time slot created", amenitiesService.createTimeSlot(id, request)));
    }

    @PutMapping("/time-slots/{id}")
    public ResponseEntity<ApiResponse<AmenityTimeSlotResponse>> updateTimeSlot(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityTimeSlotUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity time slot updated", amenitiesService.updateTimeSlot(id, request)));
    }

    @DeleteMapping("/time-slots/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimeSlot(@PathVariable UUID id) {
        amenitiesService.deleteTimeSlot(id);
        return ResponseEntity.ok(ApiResponse.ok("Amenity time slot deleted", null));
    }

    @PostMapping("/time-slots/{id}/activate")
    public ResponseEntity<ApiResponse<AmenityTimeSlotResponse>> activateTimeSlot(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity time slot activated", amenitiesService.activateTimeSlot(id)));
    }

    @PostMapping("/time-slots/{id}/deactivate")
    public ResponseEntity<ApiResponse<AmenityTimeSlotResponse>> deactivateTimeSlot(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity time slot deactivated", amenitiesService.deactivateTimeSlot(id)));
    }

    @GetMapping("/amenities/{id}/pricing")
    public ResponseEntity<ApiResponse<List<AmenityPricingResponse>>> listPricing(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity pricing fetched", amenitiesService.listPricing(id)));
    }

    @PostMapping("/amenities/{id}/pricing")
    public ResponseEntity<ApiResponse<AmenityPricingResponse>> createPricing(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityPricingCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity pricing created", amenitiesService.createPricing(id, request)));
    }

    @PutMapping("/pricing/{id}")
    public ResponseEntity<ApiResponse<AmenityPricingResponse>> updatePricing(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityPricingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity pricing updated", amenitiesService.updatePricing(id, request)));
    }

    @DeleteMapping("/pricing/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePricing(@PathVariable UUID id) {
        amenitiesService.deletePricing(id);
        return ResponseEntity.ok(ApiResponse.ok("Amenity pricing deleted", null));
    }

    @PostMapping("/amenity-bookings")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> createBooking(
            @RequestParam UUID amenityId,
            @Valid @RequestBody AmenityBookingCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking created", amenitiesService.createBooking(amenityId, request)));
    }

    @GetMapping("/amenity-bookings")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityBookingResponse>>> listBookings(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity bookings fetched", amenitiesService.listBookings(pageable)));
    }

    @GetMapping("/amenity-bookings/{id}")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> getBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking fetched", amenitiesService.getBooking(id)));
    }

    @PutMapping("/amenity-bookings/{id}")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> updateBooking(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityBookingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking updated", amenitiesService.updateBooking(id, request)));
    }

    @DeleteMapping("/amenity-bookings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable UUID id) {
        amenitiesService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking deleted", null));
    }

    @PostMapping("/amenity-bookings/{id}/approve")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> approveBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking approved", amenitiesService.approveBooking(id)));
    }

    @PostMapping("/amenity-bookings/{id}/reject")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> rejectBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking rejected", amenitiesService.rejectBooking(id)));
    }

    @PostMapping("/amenity-bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> cancelBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking cancelled", amenitiesService.cancelBooking(id)));
    }

    @PostMapping("/amenity-bookings/{id}/complete")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> completeBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking completed", amenitiesService.completeBooking(id)));
    }

    @GetMapping("/amenity-bookings/amenity/{amenityId}")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityBookingResponse>>> listBookingsByAmenity(
            @PathVariable UUID amenityId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Amenity bookings by amenity fetched",
                amenitiesService.listBookingsByAmenity(amenityId, pageable)));
    }

    @GetMapping("/amenity-bookings/my-bookings")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityBookingResponse>>> listMyBookings(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("My amenity bookings fetched", amenitiesService.listMyBookings(pageable)));
    }

    @GetMapping("/amenity-bookings/pending-approval")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityBookingResponse>>> listPendingApprovalBookings(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Pending amenity bookings fetched",
                amenitiesService.listPendingApprovalBookings(pageable)));
    }

    @GetMapping("/amenity-bookings/date/{date}")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityBookingResponse>>> listBookingsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity bookings by date fetched", amenitiesService.listBookingsByDate(date, pageable)));
    }

    @GetMapping("/amenity-bookings/check-availability")
    public ResponseEntity<ApiResponse<AmenityAvailabilityResponse>> checkAvailability(
            @RequestParam UUID amenityId,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Amenity availability checked",
                amenitiesService.checkAvailability(amenityId, startTime, endTime)));
    }

    // Legacy endpoints retained for compatibility.
    @PostMapping("/amenities/{id}/book")
    public ResponseEntity<ApiResponse<AmenityBookingResponse>> book(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityBookingCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booked", amenitiesService.book(id, request)));
    }

    @GetMapping("/amenities/bookings")
    public ResponseEntity<ApiResponse<PagedResponse<AmenityBookingResponse>>> listBookingsLegacy(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity bookings fetched", amenitiesService.listBookings(pageable)));
    }

    @GetMapping("/amenities/{id}/rules")
    public ResponseEntity<ApiResponse<List<AmenityBookingRuleResponse>>> listRules(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking rules fetched", amenitiesService.listRules(id)));
    }

    @PostMapping("/amenities/{id}/rules")
    public ResponseEntity<ApiResponse<AmenityBookingRuleResponse>> createRule(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityBookingRuleCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking rule created", amenitiesService.createRule(id, request)));
    }

    @PutMapping("/booking-rules/{id}")
    public ResponseEntity<ApiResponse<AmenityBookingRuleResponse>> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityBookingRuleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking rule updated", amenitiesService.updateRule(id, request)));
    }

    @DeleteMapping("/booking-rules/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable UUID id) {
        amenitiesService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.ok("Amenity booking rule deleted", null));
    }

    @GetMapping("/amenities/{id}/cancellation-policy")
    public ResponseEntity<ApiResponse<AmenityCancellationPolicyResponse>> getCancellationPolicy(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Amenity cancellation policy fetched",
                amenitiesService.getCancellationPolicy(id)));
    }

    @PostMapping("/amenities/{id}/cancellation-policy")
    public ResponseEntity<ApiResponse<AmenityCancellationPolicyResponse>> createCancellationPolicy(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityCancellationPolicyCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Amenity cancellation policy created",
                amenitiesService.createCancellationPolicy(id, request)));
    }

    @PutMapping("/cancellation-policy/{id}")
    public ResponseEntity<ApiResponse<AmenityCancellationPolicyResponse>> updateCancellationPolicy(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityCancellationPolicyUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Amenity cancellation policy updated",
                amenitiesService.updateCancellationPolicy(id, request)));
    }

    @DeleteMapping("/cancellation-policy/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCancellationPolicy(@PathVariable UUID id) {
        amenitiesService.deleteCancellationPolicy(id);
        return ResponseEntity.ok(ApiResponse.ok("Amenity cancellation policy deleted", null));
    }
}
