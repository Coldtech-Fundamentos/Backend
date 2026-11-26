package com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.services.TokenServiceImpl;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedIdentity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

class JwtIdentityModeTests {
    private static final String SECRET = "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz";
    private final UserRepository repository = mock(UserRepository.class);
    private final UserDetailsService users = mock(UserDetailsService.class);
    private final TokenServiceImpl tokens = new TokenServiceImpl(repository);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        verifyNoInteractions(repository, users);
    }

    private String token(Object id, Object role, String subject, Date expiration, String key) {
        return Jwts.builder().subject(subject).claim("userId", id).claim("role", role)
                .expiration(expiration).signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private MockHttpServletResponse authenticate(String token) throws Exception {
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(tokens, "secret", SECRET);
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("role", "Owner");
        request.addParameter("userId", "999");
        var response = new MockHttpServletResponse();
        new BearerAuthorizationRequestFilter(tokens, users, true)
                .doFilter(request, response, (incoming, outgoing) -> {});
        return response;
    }

    @Test
    void trustedClaimsSupplyIdentityAndExactAuthoritiesWithoutDatabaseLookup() throws Exception {
        for (String role : List.of("Owner", "Provider")) {
            assertThat(authenticate(token(123, role, "iam-only", new Date(System.currentTimeMillis() + 60000),
                    SECRET)).getStatus()).isEqualTo(200);
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            var principal = (AuthenticatedIdentity) authentication.getPrincipal();
            assertThat(principal.getUserId()).isEqualTo(123);
            assertThat(principal.getUsername()).isEqualTo("iam-only");
            assertThat(authentication.getAuthorities()).extracting(authority -> authority.getAuthority())
                    .containsExactly(role);
        }
    }

    @Test
    void absentUnknownOrMalformedRolesCannotReceiveDefaultPermissions() throws Exception {
        for (Object role : List.of("", "owner", "Admin", List.of("Owner"), true, 1)) {
            assertRejected(token(123, role, "iam-only", future(), SECRET));
        }
        assertRejected(token(123, null, "iam-only", future(), SECRET));
    }

    @Test
    void malformedIdentityExpiredAndInvalidSignaturesAreRejected() throws Exception {
        for (Object id : List.of(0, -1, "123", 1.5, 2147483648L, true)) {
            assertRejected(token(id, "Owner", "iam-only", future(), SECRET));
        }
        assertRejected(token(null, "Owner", "iam-only", future(), SECRET));
        assertRejected(token(123, "Owner", "", future(), SECRET));
        assertRejected(token(123, "Owner", "iam-only", null, SECRET));
        assertRejected(token(123, "Owner", "iam-only", new Date(0), SECRET));
        assertRejected(token(123, "Owner", "iam-only", future(), SECRET + "-wrong"));
        assertRejected("invalid");
    }

    private Date future() {
        return new Date(System.currentTimeMillis() + 60000);
    }

    private void assertRejected(String token) throws Exception {
        var response = authenticate(token);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
