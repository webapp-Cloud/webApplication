package com.rio_rishabhNEU.UserApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/healthz").permitAll()
                        .requestMatchers("/v1/verifyEmail").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/user").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {}); // Using the new lambda-based configuration

        return http.build();
    }
}