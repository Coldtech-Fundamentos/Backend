package com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.services;

import com.frigora.frigoraplatform.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.frigora.frigoraplatform.iam.application.internal.outboundservices.hashing.HashingService;
import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;
import com.frigora.frigoraplatform.iam.domain.model.commands.SignInCommand;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class TokenServiceImplTests {
    private static final String SECRET = "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz";
    private UserRepository repository;
    private TokenServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new TokenServiceImpl(repository);
        ReflectionTestUtils.setField(service, "secret", SECRET);
        ReflectionTestUtils.setField(service, "expirationDays", 7);
        user = mock(User.class);
        when(user.getId()).thenReturn(123);
        when(user.getUsername()).thenReturn("owner@example.test");
        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
    }

    @Test
    void includesRealUserIdAndPreservesClaimsSignatureAndLifetime() {
        var before = new Date(System.currentTimeMillis() - 1000);
        var token = service.generateToken(user);
        var claims = claims(token);
        assertThat(claims.get("userId", Integer.class)).isEqualTo(123);
        assertThat(claims.getSubject()).isEqualTo(user.getUsername());
        assertThat(claims.getIssuedAt()).isBetween(before, new Date());
        assertThat(claims.getExpiration()).isEqualTo(DateUtils.addDays(claims.getIssuedAt(), 7));
        var parsed = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token);
        var legacy = Jwts.builder().subject(user.getUsername()).issuedAt(new Date())
                .expiration(DateUtils.addDays(new Date(), 7))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertThat(parsed.getHeader().getAlgorithm()).isEqualTo(Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(legacy).getHeader().getAlgorithm());
        assertThat(service.validateToken(token)).isTrue();
        assertThat(service.getUsernameFromToken(token)).isEqualTo(user.getUsername());
        verifyNoInteractions(repository);
    }

    @Test
    void otherGenerationOverloadsResolveThePersistedUserId() {
        assertThat(claims(service.generateToken(user.getUsername())).get("userId", Integer.class)).isEqualTo(123);
        var authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of());
        assertThat(claims(service.generateToken(authentication)).get("userId", Integer.class)).isEqualTo(123);
    }

    @Test
    void loginUsesThePersistedUserAndKeepsItsResult() {
        var hashing = mock(HashingService.class);
        when(user.getPassword()).thenReturn("stored-hash");
        when(hashing.matches("password", "stored-hash")).thenReturn(true);
        var commands = new UserCommandServiceImpl(repository, service, hashing);
        var result = commands.handle(new SignInCommand(user.getUsername(), "password")).orElseThrow();
        assertThat(result.getLeft()).isSameAs(user);
        assertThat(claims(result.getRight()).get("userId", Integer.class)).isEqualTo(123);
    }

    @Test
    void currentFilterAcceptsNewAndLegacyTokensWithoutLoggingCredentials(CapturedOutput output) throws Exception {
        var legacy = Jwts.builder().subject(user.getUsername()).issuedAt(new Date())
                .expiration(DateUtils.addDays(new Date(), 7))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        var current = service.generateToken(user);
        var details = mock(UserDetailsService.class);
        var principal = new org.springframework.security.core.userdetails.User(
                user.getUsername(), "stored-hash", List.of());
        when(details.loadUserByUsername(user.getUsername())).thenReturn(principal);
        var filter = new BearerAuthorizationRequestFilter(service, details);
        try {
            for (var token : List.of(current, legacy)) {
                var request = new MockHttpServletRequest();
                request.addHeader("Authorization", "Bearer " + token);
                filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
                    assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
                    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(user.getUsername());
                });
                SecurityContextHolder.clearContext();
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
        assertThat(output.getAll()).doesNotContain(current, legacy, SECRET, "stored-hash");
    }

    @Test
    void failuresDoNotLogUntrustedTokenOrExceptionDetails(CapturedOutput output) throws Exception {
        var malformed = "sensitive-invalid-token";
        assertThat(service.validateToken(malformed)).isFalse();
        var expired = Jwts.builder().subject("owner").expiration(new Date(0))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertThat(service.validateToken(expired)).isFalse();
        var token = service.generateToken(user);
        var details = mock(UserDetailsService.class);
        when(details.loadUserByUsername(user.getUsername())).thenThrow(new IllegalStateException(token + SECRET));
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        try {
            new BearerAuthorizationRequestFilter(service, details).doFilter(request,
                    new MockHttpServletResponse(), (req, res) ->
                            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull());
        } finally {
            SecurityContextHolder.clearContext();
        }
        assertThat(output.getAll()).doesNotContain(malformed, expired, token, SECRET);
    }
}
