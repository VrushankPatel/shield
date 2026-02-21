package com.shield.module.marketplace.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "carpool_listing")
public class CarpoolListingEntity extends TenantAwareEntity {

    @Column(name = "posted_by", columnDefinition = "uuid")
    private UUID postedBy;

    @Column(name = "route_from", nullable = false, length = 255)
    private String routeFrom;

    @Column(name = "route_to", nullable = false, length = 255)
    private String routeTo;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "days_of_week", nullable = false, length = 100)
    private String daysOfWeek;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Column(name = "contact_preference", length = 50)
    private String contactPreference;

    @Column(nullable = false)
    private boolean active = true;
}
