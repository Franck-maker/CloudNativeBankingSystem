package com.bank.banking_system.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs
            .cors(Customizer.withDefaults()) // Enable CORS with default settings, allow cross-origin requests from Angular frontend
            .authorizeHttpRequests(auth -> auth
                // RBAC RULE 1 : Only ADMINs can get the full list of accounts
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts").hasRole("ADMIN")
                // RBAC RULE 2 : USERs and ADMINs can do everything else (create accounts, get account by id, transfer money)
                .requestMatchers("/api/v1/accounts/**").hasAnyRole("USER", "ADMIN")

                //Allow Swagger UI to bypass secrurity so we can test our API easily
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated() // All other requests require authentication
            )
            // Use basic Authentication for our MVP
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    // Use industry-standard BCrypt password encoder for hashing passwords
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();      
    }
}
