package com.shield.bootstrap;

import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminRunner implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${shield.bootstrap.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${shield.bootstrap.tenant-name:}")
    private String tenantName;

    @Value("${shield.bootstrap.tenant-address:}")
    private String tenantAddress;

    @Value("${shield.bootstrap.admin-name:Shield Admin}")
    private String adminName;

    @Value("${shield.bootstrap.admin-email:}")
    private String adminEmail;

    @Value("${shield.bootstrap.admin-password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapEnabled) {
            return;
        }

        if (tenantRepository.count() > 0 || userRepository.count() > 0) {
            log.info("Bootstrap skipped because data already exists");
            return;
        }

        if (!StringUtils.hasText(tenantName) || !StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.warn("Bootstrap skipped. Required properties missing for tenant/admin setup.");
            return;
        }

        TenantEntity tenant = new TenantEntity();
        tenant.setName(tenantName);
        tenant.setAddress(tenantAddress);
        tenant = tenantRepository.save(tenant);

        UserEntity admin = new UserEntity();
        admin.setTenantId(tenant.getId());
        admin.setName(adminName);
        admin.setEmail(adminEmail.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        log.warn("Bootstrap admin created for tenant {} with email {}. Disable bootstrap after first run.", tenantName, adminEmail);
    }
}
