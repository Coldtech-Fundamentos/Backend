package com.frigora.frigoraplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.frigora.frigoraplatform.assetsmanagement.domain.model.queries.GetSitesByOwnerQuery;
import com.frigora.frigoraplatform.assetsmanagement.domain.services.SiteCommandService;
import com.frigora.frigoraplatform.assetsmanagement.domain.services.SiteQueryService;
import com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.SiteController;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.configuration.WebSecurityConfiguration;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.pipeline.UnauthorizedRequestHandlerEntryPoint;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.services.UserDetailsServiceImpl;
import com.frigora.frigoraplatform.iam.infrastructure.hashing.bcrypt.services.HashingServiceImpl;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.services.TokenServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

class MonolithJwtIdentityIntegrationTests {
    private static final String SECRET = "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz";
    private static final int IAM_ONLY_USER_ID = 731;

    @Test
    void iamIssuedIdentityAuthenticatesAndScopesMonolithSiteQueryWithoutLocalUserLookup()
            throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new org.springframework.core.env.MapPropertySource(
                                    "test-issuer",
                                    java.util.Map.of(
                                            "authorization.jwt.secret", SECRET,
                                            "authorization.jwt.expiration.days", "7",
                                            "authorization.jwt.identity-enabled", "true")));
            context.register(MonolithSecurity.class);
            context.refresh();

            var mvc =
                    MockMvcBuilders.webAppContextSetup(context)
                            .addFilters(context.getBean(FilterChainProxy.class))
                            .build();
            var response =
                    mvc.perform(
                                    get("/api/v1/sites")
                                            .header("Authorization", "Bearer " + iamToken()))
                            .andReturn()
                            .getResponse();

            assertThat(response.getStatus()).isEqualTo(200);
            verify(context.getBean(SiteQueryService.class))
                    .handle(new GetSitesByOwnerQuery((long) IAM_ONLY_USER_ID));
            verify(context.getBean(UserRepository.class), never())
                    .findByUsername(anyString());

            var unauthenticated =
                    mvc.perform(get("/api/v1/sites")).andReturn().getResponse();
            assertThat(unauthenticated.getStatus()).isEqualTo(401);
            verifyNoMoreInteractions(context.getBean(SiteQueryService.class));
        }
    }

    private String iamToken() {
        var key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("iam-only-user")
                .claim("userId", IAM_ONLY_USER_ID)
                .claim("role", "Owner")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({
        WebSecurityConfiguration.class,
        UnauthorizedRequestHandlerEntryPoint.class,
        UserDetailsServiceImpl.class,
        HashingServiceImpl.class,
        SiteController.class
    })
    static class MonolithSecurity {
        @Bean
        UserRepository monolithUsers() {
            return mock(UserRepository.class);
        }

        @Bean
        TokenServiceImpl tokens(UserRepository repository) {
            var tokens = new TokenServiceImpl(repository);
            ReflectionTestUtils.setField(tokens, "secret", SECRET);
            return tokens;
        }

        @Bean
        SiteCommandService siteCommandService() {
            return mock(SiteCommandService.class);
        }

        @Bean
        SiteQueryService siteQueryService() {
            var queryService = mock(SiteQueryService.class);
            when(queryService.handle(any(GetSitesByOwnerQuery.class))).thenReturn(List.of());
            return queryService;
        }
    }
}
