package com.frigora.frigoraplatform.shared.infrastructure.security;

import org.springframework.security.core.Authentication;

public final class AuthenticatedUser {
    private AuthenticatedUser() {}

    public static int requireUserId(Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof JwtIdentity identity)) {
            throw new IllegalStateException("An authenticated IAM identity is required");
        }
        return identity.userId();
    }
}
