package com.frigora.frigoraplatform.shared.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.List;

/** Only claims verified by the local JWT validator; IAM does not currently issue roles. */
public record JwtIdentity(Integer userId, String username) implements AuthenticatedIdentity {
    @Override public Integer getUserId() { return userId; }
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return null; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
}
