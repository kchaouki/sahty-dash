package com.sahty.fs.security;

import com.sahty.fs.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<SahtyUserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SahtyUserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<User> getCurrentUserEntity() {
        return getCurrentUser().map(SahtyUserPrincipal::getUser);
    }

    public static Optional<String> getCurrentTenantId() {
        return getCurrentUser().map(SahtyUserPrincipal::getTenantId);
    }

    public static boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    public static boolean hasPermission(String permission) {
        return getCurrentUser().map(u -> u.hasPermission(permission)).orElse(false);
    }

    public static boolean isSuperAdmin() {
        return getCurrentUser()
                .map(u -> u.getUser().getSystemRole() == User.UserRole.SUPER_ADMIN)
                .orElse(false);
    }

    public static boolean isTenantSuperAdmin() {
        return getCurrentUser()
                .map(u -> u.getUser().getSystemRole() == User.UserRole.TENANT_SUPERADMIN ||
                          u.getUser().getSystemRole() == User.UserRole.SUPER_ADMIN)
                .orElse(false);
    }
}
