package com.rio_rishabhNEU.UserApp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sns.SnsClient;
import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class SNSTestConfig {

    @Bean
    @Primary
    public SnsClient snsClient() {
        return mock(SnsClient.class);
    }
}