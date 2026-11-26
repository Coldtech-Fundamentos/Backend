package com.frigora.frigoraplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.frigora.frigoraplatform.iam.application.internal.queryservices.UserQueryServiceImpl;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.configuration.WebSecurityConfiguration;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.pipeline.UnauthorizedRequestHandlerEntryPoint;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.services.UserDetailsServiceImpl;
import com.frigora.frigoraplatform.iam.infrastructure.hashing.bcrypt.services.HashingServiceImpl;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.services.TokenServiceImpl;
import com.frigora.frigoraplatform.iam.interfaces.rest.UsersController;
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

public final class MonolithCompatibilityAssertions {
    private MonolithCompatibilityAssertions() {}

    public static void verifyNewIamUserIsRejected(String token) throws Exception {
        verifyMode(token, false);
        verifyMode(token, true);
    }

    private static void verifyMode(String token, boolean integrated) throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(
                    new org.springframework.core.env.MapPropertySource("test-issuer", java.util.Map.of(
                            "authorization.jwt.secret", "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz",
                            "authorization.jwt.expiration.days", "7",
                            "authorization.jwt.identity-enabled", Boolean.toString(integrated))));
            context.register(MonolithSecurity.class);
            context.refresh();
            var mvc = MockMvcBuilders.webAppContextSetup(context)
                    .addFilters(context.getBean(FilterChainProxy.class)).build();
            var response = mvc.perform(get("/api/v1/users")
                    .header("Authorization", "Bearer " + token)).andReturn().getResponse();
            assertThat(response.getStatus()).isEqualTo(integrated ? 200 : 401);
            if (integrated) {
                verify(context.getBean(UserRepository.class), never()).findByUsername(anyString());
                verify(context.getBean(UserRepository.class)).findAll();
            } else {
                verify(context.getBean(UserRepository.class)).findByUsername(anyString());
                verify(context.getBean(UserRepository.class), never()).findAll();
            }
        }
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({WebSecurityConfiguration.class, UnauthorizedRequestHandlerEntryPoint.class,
        UserDetailsServiceImpl.class, HashingServiceImpl.class, UsersController.class,
        UserQueryServiceImpl.class})
    static class MonolithSecurity {
        @Bean
        UserRepository monolithUsers() {
            return mock(UserRepository.class);
        }

        @Bean
        TokenServiceImpl tokens(UserRepository repository) {
            var tokens = new TokenServiceImpl(repository);
            ReflectionTestUtils.setField(tokens, "secret",
                    "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz");
            return tokens;
        }
    }
}
