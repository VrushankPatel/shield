package com.shield.module.amenities.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amenity_booking")
public class AmenityBookingEntity extends TenantAwareEntity {

    @Column(name = "booking_number", nullable = false, length = 100)
    private String bookingNumber;

    @Column(name = "amenity_id", nullable = false, columnDefinition = "uuid")
    private UUID amenityId;

    @Column(name = "time_slot_id", columnDefinition = "uuid")
    private UUID timeSlotId;

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(name = "booked_by", columnDefinition = "uuid")
    private UUID bookedBy;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmenityBookingStatus status;

    @Column(name = "number_of_persons")
    private Integer numberOfPersons;

    @Column(length = 255)
    private String purpose;

    @Column(name = "booking_amount", precision = 12, scale = 2)
    private BigDecimal bookingAmount;

    @Column(name = "security_deposit", precision = 12, scale = 2)
    private BigDecimal securityDeposit;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50)
    private AmenityPaymentStatus paymentStatus;

    @Column(name = "approved_by", columnDefinition = "uuid")
    private UUID approvedBy;

    @Column(name = "approval_date")
    private Instant approvalDate;

    @Column(name = "cancellation_date")
    private Instant cancellationDate;

    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;

    @Column(length = 500)
    private String notes;
}
