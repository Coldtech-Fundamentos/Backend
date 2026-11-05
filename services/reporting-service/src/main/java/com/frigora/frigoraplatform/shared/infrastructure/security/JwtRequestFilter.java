package com.frigora.frigoraplatform.shared.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class JwtRequestFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtParser jwtParser;

    JwtRequestFilter(String secret) {
        var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        jwtParser = Jwts.parser().verifyWith(key).build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
                    throw new IllegalArgumentException("A Bearer token is required");
                }
                var claims =
                        jwtParser
                                .parseSignedClaims(header.substring(BEARER_PREFIX.length()))
                                .getPayload();
                Object value = claims.get("userId");
                if (claims.getExpiration() == null
                        || claims.getSubject() == null
                        || claims.getSubject().isBlank()
                        || !(value instanceof Integer || value instanceof Long)
                        || ((Number) value).longValue() <= 0
                        || ((Number) value).longValue() > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException();
                }
                var identity = new JwtIdentity(((Number) value).intValue(), claims.getSubject());
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                identity, null, identity.getAuthorities()));
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "Bearer");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
