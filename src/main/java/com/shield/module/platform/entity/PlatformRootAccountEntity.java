package com.shield.module.platform.entity;

import com.shield.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "platform_root_account")
public class PlatformRootAccountEntity extends BaseEntity {

    @Column(name = "login_id", nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String mobile;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = true;

    @Column(name = "mobile_verified", nullable = false)
    private boolean mobileVerified = true;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired = true;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(nullable = false)
    private boolean active = true;
}
