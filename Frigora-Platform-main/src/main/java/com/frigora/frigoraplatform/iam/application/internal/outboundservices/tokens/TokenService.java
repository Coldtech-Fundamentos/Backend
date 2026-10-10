package com.frigora.frigoraplatform.iam.application.internal.outboundservices.tokens;

import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;

public interface TokenService {

    /**
     * Generates a JWT token for the specified user.
     * @param user The user for which to create the token.
     * @return The generated JWT token string.
     */
    String generateToken(User user);

    /**
     * Validates the provided JWT token and returns the username when valid.
     * @param token The JWT token to validate.
     * @return The username if the token is valid; otherwise null.
     */
    String getUsernameFromToken(String token);

    /**
     * Validates if the provided JWT token is valid.
     * @param token The JWT token to validate.
     * @return True if the token is valid; otherwise false.
     */
    boolean validateToken(String token);
}
