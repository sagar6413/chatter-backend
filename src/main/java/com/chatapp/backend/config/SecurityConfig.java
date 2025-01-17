package com.chatapp.backend.config;

import com.chatapp.backend.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Slf4j
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINTS = {"/api/v1/users/auth/register", "/api/v1/users/auth/login", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**"};
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                   .csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_ENDPOINTS)
                                                      .permitAll()
                                                      .anyRequest()
                                                      .authenticated())
                   .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                   .authenticationProvider(authenticationProvider)
                   .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                   .logout(logout -> logout.logoutUrl("/api/v1/users/auth/logout")
                                           .addLogoutHandler(logoutHandler)
                                           .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()))
                   .build();
    }
}
