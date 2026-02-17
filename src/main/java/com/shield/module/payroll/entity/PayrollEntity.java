package com.shield.module.payroll.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payroll")
public class PayrollEntity extends TenantAwareEntity {

    @Column(name = "staff_id", nullable = false, columnDefinition = "uuid")
    private UUID staffId;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(name = "working_days", nullable = false)
    private int workingDays;

    @Column(name = "present_days", nullable = false)
    private int presentDays;

    @Column(name = "gross_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "total_deductions", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PayrollStatus status;

    @Column(name = "payslip_url", length = 2000)
    private String payslipUrl;
}
