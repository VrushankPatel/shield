package com.shield.module.amenities.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.amenities.dto.AmenityBookingCreateRequest;
import com.shield.module.amenities.dto.AmenityBookingResponse;
import com.shield.module.amenities.dto.AmenityCreateRequest;
import com.shield.module.amenities.dto.AmenityResponse;
import com.shield.module.amenities.entity.AmenityBookingEntity;
import com.shield.module.amenities.entity.AmenityBookingStatus;
import com.shield.module.amenities.entity.AmenityEntity;
import com.shield.module.amenities.repository.AmenityBookingRepository;
import com.shield.module.amenities.repository.AmenityRepository;
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

@ExtendWith(MockitoExtension.class)
class AmenitiesServiceTest {

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private AmenityBookingRepository amenityBookingRepository;

    @Mock
    private AuditLogService auditLogService;

    private AmenitiesService amenitiesService;

    @BeforeEach
    void setUp() {
        amenitiesService = new AmenitiesService(amenityRepository, amenityBookingRepository, auditLogService);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createShouldPersistAmenity() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(amenityRepository.save(any(AmenityEntity.class))).thenAnswer(invocation -> {
            AmenityEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        AmenityResponse response = amenitiesService.create(new AmenityCreateRequest("Banquet", 200, true));

        assertEquals(tenantId, response.tenantId());
        assertEquals("Banquet", response.name());
        assertEquals(true, response.requiresApproval());
    }

    @Test
    void bookShouldSetPendingWhenAmenityNeedsApproval() {
        UUID amenityId = UUID.randomUUID();

        AmenityEntity amenity = new AmenityEntity();
        amenity.setId(amenityId);
        amenity.setTenantId(UUID.randomUUID());
        amenity.setRequiresApproval(true);

        when(amenityRepository.findByIdAndDeletedFalse(amenityId)).thenReturn(Optional.of(amenity));
        when(amenityBookingRepository.save(any(AmenityBookingEntity.class))).thenAnswer(invocation -> {
            AmenityBookingEntity booking = invocation.getArgument(0);
            booking.setId(UUID.randomUUID());
            return booking;
        });

        AmenityBookingResponse response = amenitiesService.book(
                amenityId,
                new AmenityBookingCreateRequest(
                        UUID.randomUUID(),
                        Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(7200),
                        "family event"));

        assertEquals(AmenityBookingStatus.PENDING, response.status());
    }

    @Test
    void bookShouldThrowWhenAmenityMissing() {
        UUID amenityId = UUID.randomUUID();
        when(amenityRepository.findByIdAndDeletedFalse(amenityId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> amenitiesService.book(
                amenityId,
                new AmenityBookingCreateRequest(UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(60), null)));
    }
}
