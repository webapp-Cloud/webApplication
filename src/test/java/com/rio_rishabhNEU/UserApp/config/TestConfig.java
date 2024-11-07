package com.rio_rishabhNEU.UserApp.config;

import com.rio_rishabhNEU.UserApp.Service.S3Service;
import com.rio_rishabhNEU.UserApp.Service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import software.amazon.awssdk.services.s3.S3Client;
import org.mockito.Mockito;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public S3Client s3Client() {
        return Mockito.mock(S3Client.class);
    }

    @Bean
    @Primary
    public S3Service s3Service() {
        return Mockito.mock(S3Service.class);
    }

    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}