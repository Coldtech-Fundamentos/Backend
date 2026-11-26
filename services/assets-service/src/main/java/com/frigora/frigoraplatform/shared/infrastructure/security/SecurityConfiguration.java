package com.frigora.frigoraplatform.shared.infrastructure.security;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    UserDetailsService jwtOnlyUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Assets accepts IAM JWTs only");
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, @Value("${assets.jwt.secret}") String secret) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                        (request, response, exception) -> {
                                            response.setStatus(401);
                                            response.setHeader("WWW-Authenticate", "Bearer");
                                        }))
                .authorizeHttpRequests(
                        requests ->
                                requests.dispatcherTypeMatchers(DispatcherType.ERROR)
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .addFilterBefore(
                        new JwtRequestFilter(secret), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
