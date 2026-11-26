package com.frigora.frigoraplatform.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

public final class IamJwtContractAssertions {
    private IamJwtContractAssertions() {}

    public static void verify(String token, int userId) throws Exception {
        SecurityContextHolder.clearContext();
        try {
            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            var response = new MockHttpServletResponse();
            boolean[] reached = {false};
            new JwtRequestFilter("test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz")
                    .doFilter(request, response, (incoming, outgoing) -> reached[0] = true);
            assertThat(reached[0]).isTrue();
            var principal = (AuthenticatedIdentity) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            assertThat(principal.getUserId()).isEqualTo(userId);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
