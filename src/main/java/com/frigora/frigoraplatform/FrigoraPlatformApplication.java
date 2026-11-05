package com.frigora.frigoraplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FrigoraPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrigoraPlatformApplication.class, args);
    }

}
