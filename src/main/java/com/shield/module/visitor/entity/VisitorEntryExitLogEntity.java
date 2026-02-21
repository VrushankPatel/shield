package com.shield.module.visitor.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visitor_entry_exit_log")
public class VisitorEntryExitLogEntity extends TenantAwareEntity {

    @Column(name = "visitor_pass_id", nullable = false, columnDefinition = "uuid")
    private UUID visitorPassId;

    @Column(name = "entry_time")
    private Instant entryTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(name = "entry_gate", length = 50)
    private String entryGate;

    @Column(name = "exit_gate", length = 50)
    private String exitGate;

    @Column(name = "security_guard_entry", columnDefinition = "uuid")
    private UUID securityGuardEntry;

    @Column(name = "security_guard_exit", columnDefinition = "uuid")
    private UUID securityGuardExit;

    @Column(name = "face_capture_url", length = 1000)
    private String faceCaptureUrl;
}
