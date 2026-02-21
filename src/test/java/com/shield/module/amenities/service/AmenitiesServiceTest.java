package com.shield.module.amenities.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.amenities.dto.AmenityAvailabilityResponse;
import com.shield.module.amenities.dto.AmenityBookingCreateRequest;
import com.shield.module.amenities.dto.AmenityBookingResponse;
import com.shield.module.amenities.dto.AmenityCreateRequest;
import com.shield.module.amenities.dto.AmenityResponse;
import com.shield.module.amenities.entity.AmenityBookingEntity;
import com.shield.module.amenities.entity.AmenityBookingStatus;
import com.shield.module.amenities.entity.AmenityEntity;
import com.shield.module.amenities.repository.AmenityBookingRepository;
import com.shield.module.amenities.repository.AmenityBookingRuleRepository;
import com.shield.module.amenities.repository.AmenityCancellationPolicyRepository;
import com.shield.module.amenities.repository.AmenityPricingRepository;
import com.shield.module.amenities.repository.AmenityRepository;
import com.shield.module.amenities.repository.AmenityTimeSlotRepository;
import com.shield.security.model.ShieldPrincipal;
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AmenitiesServiceTest {

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private AmenityBookingRepository amenityBookingRepository;

    @Mock
    private AmenityTimeSlotRepository amenityTimeSlotRepository;

    @Mock
    private AmenityPricingRepository amenityPricingRepository;

    @Mock
    private AmenityBookingRuleRepository amenityBookingRuleRepository;

    @Mock
    private AmenityCancellationPolicyRepository amenityCancellationPolicyRepository;

    @Mock
    private AuditLogService auditLogService;

    private AmenitiesService amenitiesService;

    @BeforeEach
    void setUp() {
        amenitiesService = new AmenitiesService(
                amenityRepository,
                amenityBookingRepository,
                amenityTimeSlotRepository,
                amenityPricingRepository,
                amenityBookingRuleRepository,
                amenityCancellationPolicyRepository,
                auditLogService);
    }

    @AfterEach
    void clearContext() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldPersistAmenityWithDefaults() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(amenityRepository.save(any(AmenityEntity.class))).thenAnswer(invocation -> {
            AmenityEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        AmenityResponse response = amenitiesService.create(new AmenityCreateRequest(
                "Banquet",
                "BANQUET_HALL",
                "Large hall",
                200,
                "Clubhouse",
                null,
                null,
                null,
                true));

        assertEquals(tenantId, response.tenantId());
        assertEquals("Banquet", response.name());
        assertEquals(true, response.bookingAllowed());
        assertEquals(30, response.advanceBookingDays());
        assertEquals(true, response.requiresApproval());
    }

    @Test
    void createBookingShouldSetPendingWhenApprovalRequired() {
        UUID amenityId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AmenityEntity amenity = new AmenityEntity();
        amenity.setId(amenityId);
        amenity.setTenantId(tenantId);
        amenity.setRequiresApproval(true);
        amenity.setActive(true);
        amenity.setBookingAllowed(true);
        amenity.setAdvanceBookingDays(30);

        when(amenityRepository.findByIdAndDeletedFalse(amenityId)).thenReturn(Optional.of(amenity));
        when(amenityBookingRepository.countByAmenityIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndDeletedFalse(
                eq(amenityId), any(), any(), any())).thenReturn(0L);
        when(amenityBookingRepository.save(any(AmenityBookingEntity.class))).thenAnswer(invocation -> {
            AmenityBookingEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(7200);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new ShieldPrincipal(userId, tenantId, "test@shield.dev", "ADMIN"),
                null));

        AmenityBookingResponse response = amenitiesService.createBooking(
                amenityId,
                new AmenityBookingCreateRequest(UUID.randomUUID(), null, start, end, null, 80, "Wedding", null, null, "Family"));

        assertEquals(AmenityBookingStatus.PENDING, response.status());
        assertEquals(userId, response.bookedBy());
    }

    @Test
    void createBookingShouldThrowWhenOverlapExists() {
        UUID amenityId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AmenityEntity amenity = new AmenityEntity();
        amenity.setId(amenityId);
        amenity.setTenantId(tenantId);
        amenity.setRequiresApproval(false);
        amenity.setActive(true);
        amenity.setBookingAllowed(true);
        amenity.setAdvanceBookingDays(30);

        when(amenityRepository.findByIdAndDeletedFalse(amenityId)).thenReturn(Optional.of(amenity));
        when(amenityBookingRepository.countByAmenityIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndDeletedFalse(
                eq(amenityId), any(), any(), any())).thenReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new ShieldPrincipal(UUID.randomUUID(), tenantId, "test@shield.dev", "ADMIN"),
                null));

        Instant start = Instant.now().plusSeconds(3600);
        Instant end = Instant.now().plusSeconds(7200);
        AmenityBookingCreateRequest request = new AmenityBookingCreateRequest(
                UUID.randomUUID(),
                null,
                start,
                end,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(BadRequestException.class, () -> amenitiesService.createBooking(amenityId, request));
    }

    @Test
    void checkAvailabilityShouldReturnUnavailableWhenConflictsFound() {
        UUID amenityId = UUID.randomUUID();
        AmenityEntity amenity = new AmenityEntity();
        amenity.setId(amenityId);

        when(amenityRepository.findByIdAndDeletedFalse(amenityId)).thenReturn(Optional.of(amenity));
        when(amenityBookingRepository.countByAmenityIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndDeletedFalse(
                eq(amenityId), any(), any(), any())).thenReturn(2L);

        AmenityAvailabilityResponse response = amenitiesService.checkAvailability(
                amenityId,
                Instant.parse("2026-02-20T10:00:00Z"),
                Instant.parse("2026-02-20T12:00:00Z"));

        assertEquals(false, response.available());
        assertEquals(2L, response.conflictingBookings());
    }

    @Test
    void getShouldThrowWhenAmenityMissing() {
        UUID amenityId = UUID.randomUUID();
        when(amenityRepository.findByIdAndDeletedFalse(amenityId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> amenitiesService.get(amenityId));
    }
}
