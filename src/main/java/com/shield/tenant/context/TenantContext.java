package com.shield.tenant.context;

import com.shield.common.exception.UnauthorizedException;
import java.util.Optional;
import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        TENANT.set(tenantId);
    }

    public static Optional<UUID> getTenantId() {
        return Optional.ofNullable(TENANT.get());
    }

    public static UUID getRequiredTenantId() {
        return getTenantId().orElseThrow(() -> new UnauthorizedException("Tenant context is missing"));
    }

    public static void clear() {
        TENANT.remove();
    }
}
