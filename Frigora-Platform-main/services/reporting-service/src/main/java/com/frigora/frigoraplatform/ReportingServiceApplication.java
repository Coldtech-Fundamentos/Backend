package com.frigora.frigoraplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing(dateTimeProviderRef = "auditDateTimeProvider")
public class ReportingServiceApplication {
    @Bean
    DateTimeProvider auditDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static void main(String[] args) {
        SpringApplication.run(ReportingServiceApplication.class, args);
    }
}
