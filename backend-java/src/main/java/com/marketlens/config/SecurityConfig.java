package com.marketlens.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * ✅ Unified Security Configuration
 * - Single @EnableWebSecurity to avoid bean conflicts
 * - Profile-specific SecurityFilterChain beans
 * - Dev: Permissive (no auth required)
 * - Prod: Secure (JWT auth required for /api/**)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * DEV Security Filter Chain
     * - Activated when: SPRING_PROFILES_ACTIVE=dev (default)
     * - Permits all requests without authentication
     * - CORS enabled for local frontend development
     * - H2 console accessible (if present)
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔓 DEV MODE: Security disabled - All endpoints permit-all");

        http
                // Disable CSRF for dev (easier testing with Postman/curl)
                .csrf(csrf -> csrf.disable())

                // Enable CORS with config from CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Stateless sessions (no session cookies)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules: permit all
                .authorizeHttpRequests(auth -> auth
                        // Actuator endpoints (health, info)
                        .requestMatchers("/actuator/**").permitAll()

                        // H2 Console (if present) - accessible in dev
                        .requestMatchers("/h2-console/**").permitAll()

                        // All API endpoints - no auth required in dev
                        .requestMatchers("/api/**").permitAll()

                        // Everything else - permit all
                        .anyRequest().permitAll()
                );

        // Allow H2 console iframe (if H2 is used)
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * PRODUCTION Security Filter Chain
     * - Activated when: SPRING_PROFILES_ACTIVE=prod (or any non-dev profile)
     * - Requires JWT authentication for /api/**
     * - Public access to /actuator/health and /actuator/info (for health checks)
     * - Security headers enabled (HSTS, CSP, X-Frame-Options)
     */
    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔐 PROD MODE: Security enabled - JWT required for /api/**");

        http
                // Disable CSRF (using JWT, not session cookies)
                .csrf(csrf -> csrf.disable())

                // Enable CORS with config from CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Stateless sessions (JWT-based auth)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Security headers for production
                .headers(headers -> headers
                        // Prevent clickjacking
                        .frameOptions(frame -> frame.deny())

                        // XSS Protection (modern browsers use CSP)
                        .xssProtection(xss -> xss.disable())

                        // Content Security Policy
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; frame-ancestors 'none';"))

                        // HTTP Strict Transport Security (HSTS)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)) // 1 year
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public: Actuator health & info (for hosting platform health checks)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Public: Health probes for Kubernetes/Docker
                        .requestMatchers("/actuator/health/**").permitAll()

                        // Protected: All API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()

                        // Protected: Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // OAuth2 Resource Server (JWT validation)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /**
     * JWT Authentication Converter
     * - Extracts authorities from JWT claims
     * - Adds "ROLE_" prefix to authorities
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
