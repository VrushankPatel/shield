package com.shield.module.emergency.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fire_drill_record")
public class FireDrillRecordEntity extends TenantAwareEntity {

    @Column(name = "drill_date", nullable = false)
    private LocalDate drillDate;

    @Column(name = "drill_time")
    private LocalTime drillTime;

    @Column(name = "conducted_by", columnDefinition = "uuid")
    private UUID conductedBy;

    @Column(name = "evacuation_time")
    private Integer evacuationTime;

    @Column(name = "participants_count")
    private Integer participantsCount;

    @Column(length = 2000)
    private String observations;

    @Column(name = "report_url", length = 2000)
    private String reportUrl;
}
