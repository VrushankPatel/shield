package com.shield.module.amenities.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
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
import com.shield.module.amenities.entity.AmenityBookingEntity;
import com.shield.module.amenities.entity.AmenityBookingRuleEntity;
import com.shield.module.amenities.entity.AmenityBookingStatus;
import com.shield.module.amenities.entity.AmenityCancellationPolicyEntity;
import com.shield.module.amenities.entity.AmenityEntity;
import com.shield.module.amenities.entity.AmenityPaymentStatus;
import com.shield.module.amenities.entity.AmenityPricingEntity;
import com.shield.module.amenities.entity.AmenityTimeSlotEntity;
import com.shield.module.amenities.repository.AmenityBookingRepository;
import com.shield.module.amenities.repository.AmenityBookingRuleRepository;
import com.shield.module.amenities.repository.AmenityCancellationPolicyRepository;
import com.shield.module.amenities.repository.AmenityPricingRepository;
import com.shield.module.amenities.repository.AmenityRepository;
import com.shield.module.amenities.repository.AmenityTimeSlotRepository;
import com.shield.security.model.ShieldPrincipal;
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AmenitiesService {

    private static final List<AmenityBookingStatus> BLOCKING_BOOKING_STATUSES =
            List.of(AmenityBookingStatus.PENDING, AmenityBookingStatus.CONFIRMED);

    private final AmenityRepository amenityRepository;
    private final AmenityBookingRepository amenityBookingRepository;
    private final AmenityTimeSlotRepository amenityTimeSlotRepository;
    private final AmenityPricingRepository amenityPricingRepository;
    private final AmenityBookingRuleRepository amenityBookingRuleRepository;
    private final AmenityCancellationPolicyRepository amenityCancellationPolicyRepository;
    private final AuditLogService auditLogService;

    public AmenityResponse create(AmenityCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        AmenityEntity entity = new AmenityEntity();
        entity.setTenantId(tenantId);
        applyAmenityFields(entity, request.name(), request.amenityType(), request.description(), request.capacity(),
                request.location(), request.bookingAllowed(), request.advanceBookingDays(), request.active(),
                request.requiresApproval());

        AmenityEntity saved = amenityRepository.save(entity);
        auditLogService.record(tenantId, null, "AMENITY_CREATED", "amenity", saved.getId(), null);
        return toAmenityResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityResponse> list(Pageable pageable) {
        return PagedResponse.from(amenityRepository.findAllByDeletedFalse(pageable).map(this::toAmenityResponse));
    }

    @Transactional(readOnly = true)
    public AmenityResponse get(UUID id) {
        return toAmenityResponse(getAmenityEntity(id));
    }

    public AmenityResponse update(UUID id, AmenityUpdateRequest request) {
        AmenityEntity entity = getAmenityEntity(id);
        applyAmenityFields(entity, request.name(), request.amenityType(), request.description(), request.capacity(),
                request.location(), request.bookingAllowed(), request.advanceBookingDays(), request.active(),
                request.requiresApproval());

        AmenityEntity saved = amenityRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_UPDATED", "amenity", saved.getId(), null);
        return toAmenityResponse(saved);
    }

    public void delete(UUID id) {
        AmenityEntity entity = getAmenityEntity(id);
        entity.setDeleted(true);
        amenityRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "AMENITY_DELETED", "amenity", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityResponse> listByType(String amenityType, Pageable pageable) {
        return PagedResponse.from(
                amenityRepository.findAllByAmenityTypeIgnoreCaseAndDeletedFalse(amenityType, pageable)
                        .map(this::toAmenityResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityResponse> listAvailable(Pageable pageable) {
        return PagedResponse.from(
                amenityRepository.findAllByActiveTrueAndBookingAllowedTrueAndDeletedFalse(pageable)
                        .map(this::toAmenityResponse));
    }

    public AmenityResponse activate(UUID id) {
        AmenityEntity entity = getAmenityEntity(id);
        entity.setActive(true);
        AmenityEntity saved = amenityRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_ACTIVATED", "amenity", saved.getId(), null);
        return toAmenityResponse(saved);
    }

    public AmenityResponse deactivate(UUID id) {
        AmenityEntity entity = getAmenityEntity(id);
        entity.setActive(false);
        AmenityEntity saved = amenityRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_DEACTIVATED", "amenity", saved.getId(), null);
        return toAmenityResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AmenityTimeSlotResponse> listTimeSlots(UUID amenityId) {
        getAmenityEntity(amenityId);
        return amenityTimeSlotRepository.findAllByAmenityIdAndDeletedFalseOrderByStartTimeAsc(amenityId)
                .stream()
                .map(this::toAmenityTimeSlotResponse)
                .toList();
    }

    public AmenityTimeSlotResponse createTimeSlot(UUID amenityId, AmenityTimeSlotCreateRequest request) {
        AmenityEntity amenity = getAmenityEntity(amenityId);
        validateTimeSlot(request.startTime(), request.endTime());

        AmenityTimeSlotEntity entity = new AmenityTimeSlotEntity();
        entity.setTenantId(amenity.getTenantId());
        entity.setAmenityId(amenityId);
        entity.setSlotName(request.slotName());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setActive(request.active() == null || request.active());

        AmenityTimeSlotEntity saved = amenityTimeSlotRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_TIME_SLOT_CREATED", "amenity_time_slot", saved.getId(), null);
        return toAmenityTimeSlotResponse(saved);
    }

    public AmenityTimeSlotResponse updateTimeSlot(UUID id, AmenityTimeSlotUpdateRequest request) {
        AmenityTimeSlotEntity entity = getAmenityTimeSlotEntity(id);
        validateTimeSlot(request.startTime(), request.endTime());

        entity.setSlotName(request.slotName());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setActive(request.active() == null || request.active());

        AmenityTimeSlotEntity saved = amenityTimeSlotRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_TIME_SLOT_UPDATED", "amenity_time_slot", saved.getId(), null);
        return toAmenityTimeSlotResponse(saved);
    }

    public void deleteTimeSlot(UUID id) {
        AmenityTimeSlotEntity entity = getAmenityTimeSlotEntity(id);
        entity.setDeleted(true);
        amenityTimeSlotRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "AMENITY_TIME_SLOT_DELETED", "amenity_time_slot", entity.getId(), null);
    }

    public AmenityTimeSlotResponse activateTimeSlot(UUID id) {
        AmenityTimeSlotEntity entity = getAmenityTimeSlotEntity(id);
        entity.setActive(true);
        AmenityTimeSlotEntity saved = amenityTimeSlotRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_TIME_SLOT_ACTIVATED", "amenity_time_slot", saved.getId(), null);
        return toAmenityTimeSlotResponse(saved);
    }

    public AmenityTimeSlotResponse deactivateTimeSlot(UUID id) {
        AmenityTimeSlotEntity entity = getAmenityTimeSlotEntity(id);
        entity.setActive(false);
        AmenityTimeSlotEntity saved = amenityTimeSlotRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_TIME_SLOT_DEACTIVATED", "amenity_time_slot", saved.getId(), null);
        return toAmenityTimeSlotResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AmenityPricingResponse> listPricing(UUID amenityId) {
        getAmenityEntity(amenityId);
        return amenityPricingRepository.findAllByAmenityIdAndDeletedFalseOrderByCreatedAtDesc(amenityId)
                .stream()
                .map(this::toAmenityPricingResponse)
                .toList();
    }

    public AmenityPricingResponse createPricing(UUID amenityId, AmenityPricingCreateRequest request) {
        AmenityEntity amenity = getAmenityEntity(amenityId);
        AmenityTimeSlotEntity slot = getAmenityTimeSlotEntity(request.timeSlotId());
        ensureTimeSlotBelongsToAmenity(slot, amenityId);

        AmenityPricingEntity entity = new AmenityPricingEntity();
        entity.setTenantId(amenity.getTenantId());
        entity.setAmenityId(amenityId);
        entity.setTimeSlotId(request.timeSlotId());
        entity.setDayType(request.dayType());
        entity.setBasePrice(request.basePrice());
        entity.setPeakHour(Boolean.TRUE.equals(request.peakHour()));
        entity.setPeakHourMultiplier(request.peakHourMultiplier());

        AmenityPricingEntity saved = amenityPricingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_PRICING_CREATED", "amenity_pricing", saved.getId(), null);
        return toAmenityPricingResponse(saved);
    }

    public AmenityPricingResponse updatePricing(UUID id, AmenityPricingUpdateRequest request) {
        AmenityPricingEntity entity = getAmenityPricingEntity(id);
        AmenityTimeSlotEntity slot = getAmenityTimeSlotEntity(request.timeSlotId());
        ensureTimeSlotBelongsToAmenity(slot, entity.getAmenityId());

        entity.setTimeSlotId(request.timeSlotId());
        entity.setDayType(request.dayType());
        entity.setBasePrice(request.basePrice());
        entity.setPeakHour(Boolean.TRUE.equals(request.peakHour()));
        entity.setPeakHourMultiplier(request.peakHourMultiplier());

        AmenityPricingEntity saved = amenityPricingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_PRICING_UPDATED", "amenity_pricing", saved.getId(), null);
        return toAmenityPricingResponse(saved);
    }

    public void deletePricing(UUID id) {
        AmenityPricingEntity entity = getAmenityPricingEntity(id);
        entity.setDeleted(true);
        amenityPricingRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "AMENITY_PRICING_DELETED", "amenity_pricing", entity.getId(), null);
    }

    public AmenityBookingResponse book(UUID amenityId, AmenityBookingCreateRequest request) {
        return createBooking(amenityId, request);
    }

    public AmenityBookingResponse createBooking(UUID amenityId, AmenityBookingCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        AmenityEntity amenity = getAmenityEntity(amenityId);

        validateBookingWindow(request.startTime(), request.endTime());
        ensureAmenityBookable(amenity, request.startTime());

        long overlapCount = findOverlapCount(amenityId, request.startTime(), request.endTime());
        if (overlapCount > 0) {
            throw new BadRequestException("Amenity is not available for the selected time window");
        }

        if (request.timeSlotId() != null) {
            AmenityTimeSlotEntity slot = getAmenityTimeSlotEntity(request.timeSlotId());
            ensureTimeSlotBelongsToAmenity(slot, amenityId);
            if (!slot.isActive()) {
                throw new BadRequestException("Selected time slot is inactive");
            }
        }

        AmenityBookingEntity entity = new AmenityBookingEntity();
        entity.setTenantId(amenity.getTenantId());
        entity.setBookingNumber(generateBookingNumber());
        entity.setAmenityId(amenityId);
        entity.setTimeSlotId(request.timeSlotId());
        entity.setUnitId(request.unitId());
        entity.setBookedBy(principal.userId());
        entity.setBookingDate(request.bookingDate() != null
                ? request.bookingDate()
                : request.startTime().atZone(ZoneOffset.UTC).toLocalDate());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setNumberOfPersons(request.numberOfPersons());
        entity.setPurpose(request.purpose());
        entity.setBookingAmount(request.bookingAmount());
        entity.setSecurityDeposit(request.securityDeposit());
        entity.setStatus(amenity.isRequiresApproval() ? AmenityBookingStatus.PENDING : AmenityBookingStatus.CONFIRMED);
        entity.setPaymentStatus(AmenityPaymentStatus.UNPAID);
        entity.setNotes(request.notes());

        AmenityBookingEntity saved = amenityBookingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), principal.userId(), "AMENITY_BOOKING_CREATED", "amenity_booking", saved.getId(), null);
        return toAmenityBookingResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityBookingResponse> listBookings(Pageable pageable) {
        return PagedResponse.from(amenityBookingRepository.findAllByDeletedFalse(pageable).map(this::toAmenityBookingResponse));
    }

    @Transactional(readOnly = true)
    public AmenityBookingResponse getBooking(UUID id) {
        return toAmenityBookingResponse(getAmenityBookingEntity(id));
    }

    public AmenityBookingResponse updateBooking(UUID id, AmenityBookingUpdateRequest request) {
        AmenityBookingEntity entity = getAmenityBookingEntity(id);
        validateBookingWindow(request.startTime(), request.endTime());

        long overlapCount = findOverlapCountExcludingBooking(entity.getAmenityId(), entity.getId(), request.startTime(), request.endTime());
        if (overlapCount > 0) {
            throw new BadRequestException("Amenity is not available for the selected time window");
        }

        if (request.timeSlotId() != null) {
            AmenityTimeSlotEntity slot = getAmenityTimeSlotEntity(request.timeSlotId());
            ensureTimeSlotBelongsToAmenity(slot, entity.getAmenityId());
        }

        entity.setUnitId(request.unitId());
        entity.setTimeSlotId(request.timeSlotId());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setBookingDate(request.bookingDate() != null
                ? request.bookingDate()
                : request.startTime().atZone(ZoneOffset.UTC).toLocalDate());
        entity.setNumberOfPersons(request.numberOfPersons());
        entity.setPurpose(request.purpose());
        entity.setBookingAmount(request.bookingAmount());
        entity.setSecurityDeposit(request.securityDeposit());
        entity.setNotes(request.notes());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.paymentStatus() != null) {
            entity.setPaymentStatus(request.paymentStatus());
        }

        AmenityBookingEntity saved = amenityBookingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_BOOKING_UPDATED", "amenity_booking", saved.getId(), null);
        return toAmenityBookingResponse(saved);
    }

    public void deleteBooking(UUID id) {
        AmenityBookingEntity entity = getAmenityBookingEntity(id);
        entity.setDeleted(true);
        amenityBookingRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "AMENITY_BOOKING_DELETED", "amenity_booking", entity.getId(), null);
    }

    public AmenityBookingResponse approveBooking(UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        AmenityBookingEntity entity = getAmenityBookingEntity(id);
        entity.setStatus(AmenityBookingStatus.CONFIRMED);
        entity.setApprovedBy(principal.userId());
        entity.setApprovalDate(Instant.now());

        AmenityBookingEntity saved = amenityBookingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), principal.userId(), "AMENITY_BOOKING_APPROVED", "amenity_booking", saved.getId(), null);
        return toAmenityBookingResponse(saved);
    }

    public AmenityBookingResponse rejectBooking(UUID id) {
        AmenityBookingEntity entity = getAmenityBookingEntity(id);
        entity.setStatus(AmenityBookingStatus.REJECTED);
        entity.setCancellationDate(Instant.now());

        AmenityBookingEntity saved = amenityBookingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_BOOKING_REJECTED", "amenity_booking", saved.getId(), null);
        return toAmenityBookingResponse(saved);
    }

    public AmenityBookingResponse cancelBooking(UUID id) {
        AmenityBookingEntity entity = getAmenityBookingEntity(id);
        entity.setStatus(AmenityBookingStatus.CANCELLED);
        entity.setCancellationDate(Instant.now());

        AmenityBookingEntity saved = amenityBookingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_BOOKING_CANCELLED", "amenity_booking", saved.getId(), null);
        return toAmenityBookingResponse(saved);
    }

    public AmenityBookingResponse completeBooking(UUID id) {
        AmenityBookingEntity entity = getAmenityBookingEntity(id);
        entity.setStatus(AmenityBookingStatus.COMPLETED);

        AmenityBookingEntity saved = amenityBookingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_BOOKING_COMPLETED", "amenity_booking", saved.getId(), null);
        return toAmenityBookingResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityBookingResponse> listBookingsByAmenity(UUID amenityId, Pageable pageable) {
        getAmenityEntity(amenityId);
        return PagedResponse.from(
                amenityBookingRepository.findAllByAmenityIdAndDeletedFalse(amenityId, pageable)
                        .map(this::toAmenityBookingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityBookingResponse> listMyBookings(Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return PagedResponse.from(
                amenityBookingRepository.findAllByBookedByAndDeletedFalse(principal.userId(), pageable)
                        .map(this::toAmenityBookingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityBookingResponse> listPendingApprovalBookings(Pageable pageable) {
        return PagedResponse.from(
                amenityBookingRepository.findAllByStatusAndDeletedFalse(AmenityBookingStatus.PENDING, pageable)
                        .map(this::toAmenityBookingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AmenityBookingResponse> listBookingsByDate(LocalDate date, Pageable pageable) {
        return PagedResponse.from(
                amenityBookingRepository.findAllByBookingDateAndDeletedFalse(date, pageable)
                        .map(this::toAmenityBookingResponse));
    }

    @Transactional(readOnly = true)
    public AmenityAvailabilityResponse checkAvailability(UUID amenityId, Instant startTime, Instant endTime) {
        validateBookingWindow(startTime, endTime);
        getAmenityEntity(amenityId);

        long overlapCount = findOverlapCount(amenityId, startTime, endTime);
        return new AmenityAvailabilityResponse(amenityId, startTime, endTime, overlapCount == 0, overlapCount);
    }

    @Transactional(readOnly = true)
    public List<AmenityBookingRuleResponse> listRules(UUID amenityId) {
        getAmenityEntity(amenityId);
        return amenityBookingRuleRepository.findAllByAmenityIdAndDeletedFalseOrderByCreatedAtDesc(amenityId)
                .stream()
                .map(this::toAmenityBookingRuleResponse)
                .toList();
    }

    public AmenityBookingRuleResponse createRule(UUID amenityId, AmenityBookingRuleCreateRequest request) {
        AmenityEntity amenity = getAmenityEntity(amenityId);

        AmenityBookingRuleEntity entity = new AmenityBookingRuleEntity();
        entity.setTenantId(amenity.getTenantId());
        entity.setAmenityId(amenityId);
        entity.setRuleType(request.ruleType());
        entity.setRuleValue(request.ruleValue());
        entity.setActive(request.active() == null || request.active());

        AmenityBookingRuleEntity saved = amenityBookingRuleRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_BOOKING_RULE_CREATED", "amenity_booking_rule", saved.getId(), null);
        return toAmenityBookingRuleResponse(saved);
    }

    public AmenityBookingRuleResponse updateRule(UUID id, AmenityBookingRuleUpdateRequest request) {
        AmenityBookingRuleEntity entity = getAmenityBookingRuleEntity(id);
        entity.setRuleType(request.ruleType());
        entity.setRuleValue(request.ruleValue());
        entity.setActive(request.active() == null || request.active());

        AmenityBookingRuleEntity saved = amenityBookingRuleRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_BOOKING_RULE_UPDATED", "amenity_booking_rule", saved.getId(), null);
        return toAmenityBookingRuleResponse(saved);
    }

    public void deleteRule(UUID id) {
        AmenityBookingRuleEntity entity = getAmenityBookingRuleEntity(id);
        entity.setDeleted(true);
        amenityBookingRuleRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "AMENITY_BOOKING_RULE_DELETED", "amenity_booking_rule", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public AmenityCancellationPolicyResponse getCancellationPolicy(UUID amenityId) {
        getAmenityEntity(amenityId);
        AmenityCancellationPolicyEntity policy = amenityCancellationPolicyRepository
                .findFirstByAmenityIdAndDeletedFalseOrderByCreatedAtDesc(amenityId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation policy not found for amenity: " + amenityId));
        return toAmenityCancellationPolicyResponse(policy);
    }

    public AmenityCancellationPolicyResponse createCancellationPolicy(UUID amenityId, AmenityCancellationPolicyCreateRequest request) {
        AmenityEntity amenity = getAmenityEntity(amenityId);

        AmenityCancellationPolicyEntity entity = new AmenityCancellationPolicyEntity();
        entity.setTenantId(amenity.getTenantId());
        entity.setAmenityId(amenityId);
        entity.setDaysBeforeBooking(request.daysBeforeBooking());
        entity.setRefundPercentage(request.refundPercentage());

        AmenityCancellationPolicyEntity saved = amenityCancellationPolicyRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_CANCELLATION_POLICY_CREATED", "amenity_cancellation_policy", saved.getId(), null);
        return toAmenityCancellationPolicyResponse(saved);
    }

    public AmenityCancellationPolicyResponse updateCancellationPolicy(UUID id, AmenityCancellationPolicyUpdateRequest request) {
        AmenityCancellationPolicyEntity entity = getAmenityCancellationPolicyEntity(id);
        entity.setDaysBeforeBooking(request.daysBeforeBooking());
        entity.setRefundPercentage(request.refundPercentage());

        AmenityCancellationPolicyEntity saved = amenityCancellationPolicyRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "AMENITY_CANCELLATION_POLICY_UPDATED", "amenity_cancellation_policy", saved.getId(), null);
        return toAmenityCancellationPolicyResponse(saved);
    }

    public void deleteCancellationPolicy(UUID id) {
        AmenityCancellationPolicyEntity entity = getAmenityCancellationPolicyEntity(id);
        entity.setDeleted(true);
        amenityCancellationPolicyRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "AMENITY_CANCELLATION_POLICY_DELETED", "amenity_cancellation_policy", entity.getId(), null);
    }

    private AmenityEntity getAmenityEntity(UUID amenityId) {
        return amenityRepository.findByIdAndDeletedFalse(amenityId)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found: " + amenityId));
    }

    private AmenityBookingEntity getAmenityBookingEntity(UUID bookingId) {
        return amenityBookingRepository.findByIdAndDeletedFalse(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity booking not found: " + bookingId));
    }

    private AmenityTimeSlotEntity getAmenityTimeSlotEntity(UUID id) {
        return amenityTimeSlotRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity time slot not found: " + id));
    }

    private AmenityPricingEntity getAmenityPricingEntity(UUID id) {
        return amenityPricingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity pricing not found: " + id));
    }

    private AmenityBookingRuleEntity getAmenityBookingRuleEntity(UUID id) {
        return amenityBookingRuleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity booking rule not found: " + id));
    }

    private AmenityCancellationPolicyEntity getAmenityCancellationPolicyEntity(UUID id) {
        return amenityCancellationPolicyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity cancellation policy not found: " + id));
    }

    private void applyAmenityFields(
            AmenityEntity entity,
            String name,
            String amenityType,
            String description,
            Integer capacity,
            String location,
            Boolean bookingAllowed,
            Integer advanceBookingDays,
            Boolean active,
            boolean requiresApproval) {

        entity.setName(name);
        entity.setAmenityType(amenityType);
        entity.setDescription(description);
        entity.setCapacity(capacity);
        entity.setLocation(location);
        entity.setBookingAllowed(bookingAllowed == null || bookingAllowed);
        entity.setAdvanceBookingDays(advanceBookingDays == null ? 30 : advanceBookingDays);
        entity.setRequiresApproval(requiresApproval);
        entity.setActive(active == null || active);
    }

    private void validateTimeSlot(java.time.LocalTime start, java.time.LocalTime end) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("startTime must be before endTime");
        }
    }

    private void validateBookingWindow(Instant start, Instant end) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("startTime must be before endTime");
        }
    }

    private void ensureAmenityBookable(AmenityEntity amenity, Instant startTime) {
        if (!amenity.isActive()) {
            throw new BadRequestException("Amenity is inactive");
        }

        if (!amenity.isBookingAllowed()) {
            throw new BadRequestException("Amenity booking is not allowed");
        }

        long daysAhead = java.time.Duration.between(Instant.now(), startTime).toDays();
        if (daysAhead > amenity.getAdvanceBookingDays()) {
            throw new BadRequestException("Booking exceeds amenity advance booking window");
        }
    }

    private void ensureTimeSlotBelongsToAmenity(AmenityTimeSlotEntity slot, UUID amenityId) {
        if (!slot.getAmenityId().equals(amenityId)) {
            throw new BadRequestException("Time slot does not belong to amenity");
        }
    }

    private long findOverlapCount(UUID amenityId, Instant startTime, Instant endTime) {
        return amenityBookingRepository
                .countByAmenityIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndDeletedFalse(
                        amenityId,
                        endTime,
                        startTime,
                        BLOCKING_BOOKING_STATUSES);
    }

    private long findOverlapCountExcludingBooking(UUID amenityId, UUID bookingId, Instant startTime, Instant endTime) {
        return amenityBookingRepository
                .countByAmenityIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndDeletedFalse(
                        amenityId,
                        bookingId,
                        endTime,
                        startTime,
                        BLOCKING_BOOKING_STATUSES);
    }

    private String generateBookingNumber() {
        return "ABK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AmenityResponse toAmenityResponse(AmenityEntity entity) {
        return new AmenityResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getAmenityType(),
                entity.getDescription(),
                entity.getCapacity(),
                entity.getLocation(),
                entity.isBookingAllowed(),
                entity.getAdvanceBookingDays(),
                entity.isRequiresApproval(),
                entity.isActive());
    }

    private AmenityBookingResponse toAmenityBookingResponse(AmenityBookingEntity entity) {
        return new AmenityBookingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getBookingNumber(),
                entity.getAmenityId(),
                entity.getTimeSlotId(),
                entity.getUnitId(),
                entity.getBookedBy(),
                entity.getBookingDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getNumberOfPersons(),
                entity.getPurpose(),
                entity.getStatus(),
                entity.getBookingAmount(),
                entity.getSecurityDeposit(),
                entity.getPaymentStatus(),
                entity.getApprovedBy(),
                entity.getApprovalDate(),
                entity.getCancellationDate(),
                entity.getCancellationReason(),
                entity.getNotes());
    }

    private AmenityTimeSlotResponse toAmenityTimeSlotResponse(AmenityTimeSlotEntity entity) {
        return new AmenityTimeSlotResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAmenityId(),
                entity.getSlotName(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.isActive());
    }

    private AmenityPricingResponse toAmenityPricingResponse(AmenityPricingEntity entity) {
        return new AmenityPricingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAmenityId(),
                entity.getTimeSlotId(),
                entity.getDayType(),
                entity.getBasePrice(),
                entity.isPeakHour(),
                entity.getPeakHourMultiplier());
    }

    private AmenityBookingRuleResponse toAmenityBookingRuleResponse(AmenityBookingRuleEntity entity) {
        return new AmenityBookingRuleResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAmenityId(),
                entity.getRuleType(),
                entity.getRuleValue(),
                entity.isActive());
    }

    private AmenityCancellationPolicyResponse toAmenityCancellationPolicyResponse(AmenityCancellationPolicyEntity entity) {
        return new AmenityCancellationPolicyResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAmenityId(),
                entity.getDaysBeforeBooking(),
                entity.getRefundPercentage());
    }
}
