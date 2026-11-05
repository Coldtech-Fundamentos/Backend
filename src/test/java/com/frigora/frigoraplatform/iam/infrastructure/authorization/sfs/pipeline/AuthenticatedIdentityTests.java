package com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.pipeline;

import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.services.TokenServiceImpl;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedIdentity;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticatedIdentityTests {
    private static final String SECRET = "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz";
    private UserRepository repository;
    private UserDetailsService users;
    private TokenServiceImpl tokens;
    private BearerAuthorizationRequestFilter filter;
    private UserDetailsImpl principal;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        repository = mock(UserRepository.class);
        users = mock(UserDetailsService.class);
        tokens = new TokenServiceImpl(repository);
        ReflectionTestUtils.setField(tokens, "secret", SECRET);
        principal = new UserDetailsImpl("owner", "hash", List.of(new SimpleGrantedAuthority("Owner")));
        when(users.loadUserByUsername("owner")).thenReturn(principal);
        filter = new BearerAuthorizationRequestFilter(tokens, users);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        verifyNoInteractions(repository);
    }

    private String token(Object userId, String secret, Date expiration) {
        var builder = Jwts.builder().subject("owner").issuedAt(new Date()).expiration(expiration);
        if (userId != null) {
            builder.claim("userId", userId);
        }
        return builder.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
    }

    private Authentication authenticate(String token) throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        // Client-supplied identifiers must have no influence on the authenticated identity.
        request.addHeader("userId", "999");
        request.addParameter("userId", "999");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> { });
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void verifiedClaimIsAvailableWithoutChangingPrincipalTypeNameOrAuthorities() throws Exception {
        var auth = authenticate(token(123, SECRET, new Date(System.currentTimeMillis() + 60000)));
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getName()).isEqualTo("owner");
        assertThat(auth.getAuthorities()).extracting(authority -> authority.getAuthority()).containsExactly("Owner");
        assertThat(auth.getPrincipal()).isInstanceOf(UserDetailsImpl.class).isInstanceOf(AuthenticatedIdentity.class);
        assertThat(((AuthenticatedIdentity) auth.getPrincipal()).getUserId()).isEqualTo(123);
        assertThat(AuthenticatedUser.requireUserId(auth)).isEqualTo(123);
        verify(users).loadUserByUsername("owner");
        verifyNoMoreInteractions(users);
    }

    @Test
    void legacyTokenStillAuthenticatesButRequiredIdReturns401() throws Exception {
        var auth = authenticate(token(null, SECRET, new Date(System.currentTimeMillis() + 60000)));
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getName()).isEqualTo("owner");
        assertThat(auth.getAuthorities()).extracting(authority -> authority.getAuthority()).containsExactly("Owner");
        assertThat(((AuthenticatedIdentity) auth.getPrincipal()).getUserId()).isNull();
        assertThatThrownBy(() -> AuthenticatedUser.requireUserId(auth))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void invalidSignatureNeverEstablishesAuthentication() throws Exception {
        var invalid = token(123, SECRET + "other-key", new Date(System.currentTimeMillis() + 60000));
        assertThatThrownBy(() -> tokens.getUserIdFromToken(invalid)).isInstanceOf(io.jsonwebtoken.JwtException.class);
        assertThat(authenticate(invalid)).isNull();
        verifyNoInteractions(users);
    }

    @Test
    void expiredTokenNeverEstablishesAuthentication() throws Exception {
        var expired = token(123, SECRET, new Date(0));
        assertThatThrownBy(() -> tokens.getUserIdFromToken(expired)).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        assertThat(authenticate(expired)).isNull();
        verifyNoInteractions(users);
    }

    @Test
    void malformedIdsAreRejectedWithoutCoercionOrTruncation() throws Exception {
        for (Object id : List.of("123", "invalid", 0, -1, 1.5, true, 2147483648L, List.of(123))) {
            var malformed = token(id, SECRET, new Date(System.currentTimeMillis() + 60000));
            assertThatThrownBy(() -> tokens.getUserIdFromToken(malformed)).isInstanceOf(IllegalArgumentException.class);
            assertThat(authenticate(malformed)).isNull();
        }
        verifyNoInteractions(users);
    }

    @Test
    void compatibleAdapterPreservesAccountFlagsAndOriginalBuilder() {
        UserDetails source = org.springframework.security.core.userdetails.User.withUsername("owner")
                .password("hash").authorities("Owner").disabled(true).accountExpired(true)
                .accountLocked(true).credentialsExpired(true).build();
        var adapted = UsernamePasswordAuthenticationTokenBuilder.build(source, new MockHttpServletRequest(), 123);
        var identity = (UserDetailsImpl) adapted.getPrincipal();
        assertThat(identity.isEnabled()).isFalse();
        assertThat(identity.isAccountNonExpired()).isFalse();
        assertThat(identity.isAccountNonLocked()).isFalse();
        assertThat(identity.isCredentialsNonExpired()).isFalse();
        assertThat(UsernamePasswordAuthenticationTokenBuilder.build(principal, new MockHttpServletRequest())
                .getPrincipal()).isSameAs(principal);
    }

    @Test
    void requiredIdRejectsMissingOrUnauthenticatedPrincipal() {
        var identity = UserDetailsImpl.withValidatedUserId(principal, 123);
        var unauthenticated = new UsernamePasswordAuthenticationToken(identity, null);
        for (var auth : List.of(unauthenticated,
                new UsernamePasswordAuthenticationToken("owner", null, List.of()))) {
            assertThatThrownBy(() -> AuthenticatedUser.requireUserId(auth))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        }
        assertThatThrownBy(() -> AuthenticatedUser.requireUserId(null))
                .isInstanceOf(ResponseStatusException.class);
    }
}
