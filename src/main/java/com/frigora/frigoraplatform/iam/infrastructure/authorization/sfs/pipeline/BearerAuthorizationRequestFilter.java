package com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.pipeline;

import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedIdentity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bearer Authorization Request Filter.
 *
 * <p>This class is responsible for filtering requests and setting the user authentication. It
 * extends the OncePerRequestFilter class.
 *
 * @see OncePerRequestFilter
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;

    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService userDetailsService;

    private final boolean jwtIdentityEnabled;

    public BearerAuthorizationRequestFilter(
            BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this(tokenService, userDetailsService, false);
    }

    public BearerAuthorizationRequestFilter(
            BearerTokenService tokenService, UserDetailsService userDetailsService, boolean jwtIdentityEnabled) {
        this.jwtIdentityEnabled = jwtIdentityEnabled;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method is responsible for filtering requests and setting the user authentication.
     *
     * @param request The request object.
     * @param response The response object.
     * @param filterChain The filter chain object.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            LOGGER.info("FILTER EXECUTED: {}", request.getRequestURI());
            String token = tokenService.getBearerTokenFrom(request);

            if (jwtIdentityEnabled) {
                if (token == null && request.getHeader("Authorization") != null) {
                    throw new IllegalArgumentException("A Bearer token is required");
                }
                if (token != null) {
                    var identity = tokenService.getIdentityFromToken(token);
                    var auth = UsernamePasswordAuthenticationTokenBuilder.build(identity, request, identity.getUserId());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else if (token != null && tokenService.validateToken(token)) {

                String username = tokenService.getUsernameFromToken(token);
                Integer userId = tokenService.getUserIdFromToken(token);

                var userDetails = userDetailsService.loadUserByUsername(username);
                if (userDetails instanceof AuthenticatedIdentity local
                        && local.getUserId() != null) {
                    if (userId != null && !userId.equals(local.getUserId())) {
                        throw new IllegalArgumentException("Identity does not match the local user");
                    }
                    userId = local.getUserId();
                }

                var auth =
                        UsernamePasswordAuthenticationTokenBuilder.build(
                                userDetails, request, userId);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            LOGGER.error("Cannot set user authentication");
            SecurityContextHolder.clearContext();
            if (jwtIdentityEnabled) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "Bearer");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
