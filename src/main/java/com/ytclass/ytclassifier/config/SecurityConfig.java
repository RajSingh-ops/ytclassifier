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
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/video/**", "/test.mp4", "/spa.css", "/app.js", "/logo.png", "/flowchart.png", "/favicon.ico", "/how-it-works", "/privacy", "/terms").permitAll()
                .anyRequest().authenticated() 
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/") 
                .defaultSuccessUrl("/", true) 
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none'")
    )
);
        return http.build();
    }
}
