package com.ytclass.ytclassifier.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Enable CSRF globally, ignore only for stateless /api/**
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/video/**", "/test.mp4", "/spa.css", "/app.js", "/logo.png", "/favicon.ico").permitAll()
                .anyRequest().authenticated() // Require login for anything else
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/") // Redirect to landing page for login
                .defaultSuccessUrl("/", true) // Redirect to landing page on success (shows "Go to Dashboard")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}
