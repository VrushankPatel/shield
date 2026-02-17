package com.shield.module.amenities.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AmenitiesService {

    private final AmenityRepository amenityRepository;
    private final AmenityBookingRepository amenityBookingRepository;
    private final AuditLogService auditLogService;

    public AmenityResponse create(AmenityCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        AmenityEntity entity = new AmenityEntity();
        entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setCapacity(request.capacity());
        entity.setRequiresApproval(request.requiresApproval());

        AmenityEntity saved = amenityRepository.save(entity);
        auditLogService.record(tenantId, null, "AMENITY_CREATED", "amenity", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityResponse> list(Pageable pageable) {
        return PagedResponse.from(amenityRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public AmenityBookingResponse book(UUID amenityId, AmenityBookingCreateRequest request) {
        AmenityEntity amenity = amenityRepository.findByIdAndDeletedFalse(amenityId)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found: " + amenityId));

        AmenityBookingEntity booking = new AmenityBookingEntity();
        booking.setTenantId(amenity.getTenantId());
        booking.setAmenityId(amenityId);
        booking.setUnitId(request.unitId());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setNotes(request.notes());
        booking.setStatus(amenity.isRequiresApproval() ? AmenityBookingStatus.PENDING : AmenityBookingStatus.CONFIRMED);

        AmenityBookingEntity saved = amenityBookingRepository.save(booking);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_BOOKING_CREATED", "amenity_booking", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityBookingResponse> listBookings(Pageable pageable) {
        return PagedResponse.from(amenityBookingRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    private AmenityResponse toResponse(AmenityEntity entity) {
        return new AmenityResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getCapacity(),
                entity.isRequiresApproval());
    }

    private AmenityBookingResponse toResponse(AmenityBookingEntity entity) {
        return new AmenityBookingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAmenityId(),
                entity.getUnitId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getNotes());
    }
}
