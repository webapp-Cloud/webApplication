package com.rio_rishabhNEU.UserApp.config;

import com.rio_rishabhNEU.UserApp.Service.S3Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import org.mockito.Mockito;
import software.amazon.awssdk.services.sns.SnsClient;

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
    public SnsClient snsClient() {
        return Mockito.mock(SnsClient.class);
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
}