package com.shield.module.staff.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "staff_attendance")
public class StaffAttendanceEntity extends TenantAwareEntity {

    @Column(name = "staff_id", nullable = false, columnDefinition = "uuid")
    private UUID staffId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private Instant checkInTime;

    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StaffAttendanceStatus status;

    @Column(name = "marked_by", columnDefinition = "uuid")
    private UUID markedBy;
}
