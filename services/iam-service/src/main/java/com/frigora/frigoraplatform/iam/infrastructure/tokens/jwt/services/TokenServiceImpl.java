package com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.services;

import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;
import com.frigora.frigoraplatform.iam.domain.model.valueobjects.Roles;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedIdentity;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.BearerTokenService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.crypto.SecretKey;

/**
 * Token service implementation for JWT tokens. This class is responsible for generating and
 * validating JWT tokens. It uses the secret and expiration days from the application.properties
 * file.
 */
@Service
public class TokenServiceImpl implements BearerTokenService {
    private final UserRepository userRepository;

    public TokenServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private static final int TOKEN_BEGIN_INDEX = 7;

    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;

    /**
     * This method generates a JWT token from an authentication object
     *
     * @param authentication the authentication object
     * @return String the JWT token
     * @see Authentication
     */
    @Override
    public String generateToken(Authentication authentication) {
        return generateToken(authentication.getName());
    }

    /**
     * This method generates a JWT token from a username
     *
     * @param username the username
     * @return String the JWT token
     */
    public String generateToken(String username) {
        return generateToken(
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    /**
     * This method generates a JWT token from a username and a secret. It uses the default
     * expiration days from the application.properties file.
     *
     * @param username the username
     * @return String the JWT token
     */
    private String buildTokenWithDefaultParameters(String username, int userId, String role) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * This method extracts the username from a JWT token
     *
     * @param token the token
     * @return String the username
     */
    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Integer getUserIdFromToken(String token) {
        var claims = extractAllClaims(token);
        if (!claims.containsKey("userId")) {
            return null;
        }
        Object value = claims.get("userId");
        if ((value instanceof Integer || value instanceof Long)
                && ((Number) value).longValue() > 0
                && ((Number) value).longValue() <= Integer.MAX_VALUE) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Invalid userId claim");
    }

    /**
     * This method validates a JWT token
     *
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            LOGGER.info("Token is valid");
            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid JSON Web Token Signature");
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JSON Web Token");
        } catch (ExpiredJwtException e) {
            LOGGER.error("JSON Web Token is expired");
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JSON Web Token is unsupported");
        } catch (IllegalArgumentException e) {
            LOGGER.error("JSON Web Token claims string is empty");
        }
        return false;
    }

    /**
     * Extract a claim from a token
     *
     * @param token the token
     * @param claimsResolvers the claims resolver
     * @param <T> the type of the claim
     * @return T the claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Extract all claims from a token
     *
     * @param token the token
     * @return Claims the claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get the signing key
     *
     * @return SecretKey the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public AuthenticatedIdentity getIdentityFromToken(String token) {
        var claims = extractAllClaims(token);
        Object value = claims.get("userId");
        Object role = claims.get("role");
        if (claims.getExpiration() == null || claims.getSubject() == null || claims.getSubject().isBlank()
                || !(value instanceof Integer || value instanceof Long)
                || ((Number) value).longValue() <= 0
                || ((Number) value).longValue() > Integer.MAX_VALUE
                || !(role instanceof String)) {
            throw new IllegalArgumentException("Invalid identity claims");
        }
        var verifiedRole = Roles.valueOf((String) role);
        var principal = new UserDetailsImpl(
                claims.getSubject(), "", List.of(new SimpleGrantedAuthority(verifiedRole.name())));
        return UserDetailsImpl.withValidatedUserId(principal, ((Number) value).intValue());
    }

    private boolean isTokenPresentIn(String authorizationParameter) {
        return StringUtils.hasText(authorizationParameter);
    }

    private boolean isBearerTokenIn(String authorizationParameter) {
        return authorizationParameter.startsWith(BEARER_TOKEN_PREFIX);
    }

    private String extractTokenFrom(String authorizationHeaderParameter) {
        return authorizationHeaderParameter.substring(TOKEN_BEGIN_INDEX);
    }

    private String getAuthorizationParameterFrom(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_PARAMETER_NAME);
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String parameter = getAuthorizationParameterFrom(request);
        if (isTokenPresentIn(parameter) && isBearerTokenIn(parameter)) {
            return extractTokenFrom(parameter);
        }
        return null;
    }

    @Override
    public String generateToken(User user) {
        return buildTokenWithDefaultParameters(user.getUsername(), user.getId(), user.getRole().name());
    }
}
