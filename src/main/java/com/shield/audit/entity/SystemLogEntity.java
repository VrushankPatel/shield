package com.shield.audit.entity;

import com.shield.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "system_log")
public class SystemLogEntity extends BaseEntity {

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "log_level", nullable = false, length = 20)
    private String logLevel;

    @Column(name = "logger_name", nullable = false, length = 255)
    private String loggerName;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "exception_trace", columnDefinition = "text")
    private String exceptionTrace;

    @Column(length = 255)
    private String endpoint;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}
