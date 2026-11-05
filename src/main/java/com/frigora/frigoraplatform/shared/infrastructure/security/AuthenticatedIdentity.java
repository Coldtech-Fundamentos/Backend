package com.frigora.frigoraplatform.shared.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;

// Identity exposed by security, independently of the IAM implementation.
public interface AuthenticatedIdentity extends UserDetails {
    // Returns null for legacy tokens without a userId claim.
    Integer getUserId();
}
