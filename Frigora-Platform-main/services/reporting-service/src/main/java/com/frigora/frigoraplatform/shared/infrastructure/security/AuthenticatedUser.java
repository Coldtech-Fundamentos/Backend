package com.frigora.frigoraplatform.shared.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

/** Access to the identity already established by the security filter. */
public final class AuthenticatedUser {
    private AuthenticatedUser() {
    }

    public static int requireUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedIdentity identity) {
            Integer userId = identity.getUserId();
            if (userId != null && userId > 0) {
                return userId;
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A valid userId is required");
    }
}
