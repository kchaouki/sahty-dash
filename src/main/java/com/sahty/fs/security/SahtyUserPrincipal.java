package com.sahty.fs.security;

import com.sahty.fs.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class SahtyUserPrincipal implements UserDetails {

    private final User user;
    private final Set<? extends GrantedAuthority> authorities;

    public SahtyUserPrincipal(User user, Set<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public User getUser() { return user; }
    public String getTenantId() { return user.getTenant() != null ? user.getTenant().getId() : null; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isActive(); }

    public boolean hasPermission(String permission) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(permission));
    }
}
