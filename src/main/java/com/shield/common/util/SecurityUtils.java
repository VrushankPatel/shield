package com.shield.common.util;

import com.shield.common.exception.UnauthorizedException;
import com.shield.security.model.ShieldPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static ShieldPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ShieldPrincipal principal)) {
            throw new UnauthorizedException("Authenticated user not found");
        }
        return principal;
    }
}
