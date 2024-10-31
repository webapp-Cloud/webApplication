package com.rio_rishabhNEU.UserApp.config;

import com.rio_rishabhNEU.UserApp.Service.S3Service;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
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
}