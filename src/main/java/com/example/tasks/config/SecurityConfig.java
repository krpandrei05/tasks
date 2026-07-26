package com.example.tasks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuhenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuhenticationFilter) {
        this.jwtAuhenticationFilter = jwtAuhenticationFilter;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/auth/login").permitAll()
                                .requestMatchers("/auth/register").permitAll()
                                .anyRequest().authenticated()
                ).addFilterBefore(jwtAuhenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}