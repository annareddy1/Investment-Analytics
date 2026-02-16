# 🔐 Security Configuration Guide

## Overview

MarketLens uses a unified Spring Security configuration with **profile-specific beans** to avoid bean definition conflicts. This ensures only ONE `SecurityFilterChain` is active at a time based on the active Spring profile.

---

## Architecture

### Unified SecurityConfig

**File:** `backend-java/src/main/java/com/marketlens/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity  // ✅ Only ONE @EnableWebSecurity in the entire application
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Profile("dev")  // Active when: SPRING_PROFILES_ACTIVE=dev
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) { ... }

    @Bean
    @Profile("!dev")  // Active when: SPRING_PROFILES_ACTIVE=prod (or any non-dev profile)
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) { ... }
}
```

**Key Design Decisions:**
- ✅ Single `@EnableWebSecurity` annotation (no conflicts)
- ✅ Profile-specific beans with different names (`devSecurityFilterChain` vs `prodSecurityFilterChain`)
- ✅ Only ONE bean active at a time based on profile
- ✅ Shared `CorsConfigurationSource` injected via constructor
- ✅ NO `allow-bean-definition-overriding` needed

---

## Dev Profile Security

**Activated when:** `SPRING_PROFILES_ACTIVE=dev` (default)

### Configuration

```java
@Bean
@Profile("dev")
public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())  // Disabled for easier testing
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll()  // If H2 present
            .requestMatchers("/api/**").permitAll()
            .anyRequest().permitAll()
        );

    // Allow H2 console iframe
    http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    return http.build();
}
```

### Security Rules

| Endpoint Pattern | Auth Required | Notes |
|------------------|---------------|-------|
| `/actuator/**` | ❌ No | Health checks accessible |
| `/h2-console/**` | ❌ No | H2 console (if present) |
| `/api/**` | ❌ No | All API endpoints open |
| Everything else | ❌ No | Fully permissive |

### CORS (Dev)

**Allowed Origins (from application.properties):**
```properties
cors.allowed.origins=http://localhost:3000,http://127.0.0.1:3000,http://localhost:3001,http://127.0.0.1:3001
```

**Allowed Methods:**
- GET, POST, PUT, PATCH, DELETE, OPTIONS

**Allowed Headers:**
- Authorization, Content-Type, Accept, X-Requested-With, Cache-Control

**Credentials:** Allowed (cookies, auth headers)

---

## Prod Profile Security

**Activated when:** `SPRING_PROFILES_ACTIVE=prod` (or staging, test, etc.)

### Configuration

```java
@Bean
@Profile("!dev")  // Any non-dev profile
public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())  // Using JWT, not session cookies
        .cors(cors -> cors.configurationSource(corsConfigurationSource))

        // Security headers
        .headers(headers -> headers
            .frameOptions(frame -> frame.deny())
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none';"))
            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
        )

        // Authorization
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/actuator/health/**").permitAll()  // Kubernetes probes
            .requestMatchers("/api/**").authenticated()
            .anyRequest().authenticated()
        )

        // OAuth2 JWT validation
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(...)));

    return http.build();
}
```

### Security Rules

| Endpoint Pattern | Auth Required | Notes |
|------------------|---------------|-------|
| `/actuator/health` | ❌ No | Public health check |
| `/actuator/info` | ❌ No | Public app info |
| `/actuator/health/**` | ❌ No | Kubernetes probes |
| `/api/**` | ✅ Yes (JWT) | Protected API endpoints |
| Everything else | ✅ Yes (JWT) | Default: protected |

### Security Headers (Prod)

| Header | Value | Purpose |
|--------|-------|---------|
| X-Frame-Options | DENY | Prevent clickjacking |
| Content-Security-Policy | `default-src 'self'; frame-ancestors 'none'` | XSS protection |
| Strict-Transport-Security | `max-age=31536000; includeSubDomains` | Force HTTPS |

### CORS (Prod)

**Allowed Origins (from application-prod.properties):**
```properties
cors.allowed.origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
```

Should be set via environment variable in production:
```bash
CORS_ALLOWED_ORIGINS=https://app.marketlens.com,https://www.marketlens.com
```

---

## CORS Configuration

**File:** `backend-java/src/main/java/com/marketlens/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", ...));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**Environment-Driven:**
- Dev: Multiple localhost origins (3000, 3001)
- Prod: Production domain(s) from environment variable

---

## Application Properties

### application.properties (Base)

```properties
# Default profile
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}

# CORS (overridden by profile-specific files)
cors.allowed.origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://127.0.0.1:3000,http://localhost:3001,http://127.0.0.1:3001}

# OAuth2 JWT (only used in prod)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${JWT_ISSUER_URI:https://YOUR_DOMAIN.auth0.com/}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${JWT_JWK_SET_URI:https://YOUR_DOMAIN.auth0.com/.well-known/jwks.json}
```

### application-dev.properties

```properties
# No additional security config needed
# Dev profile uses permissive security by default
```

### application-prod.properties

```properties
# CORS for production
cors.allowed.origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com}

# OAuth2 JWT (required in prod)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${JWT_ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${JWT_JWK_SET_URI}
```

---

## Testing

### 1. Test Dev Mode (Permissive)

```bash
# Start in dev mode
cd backend-java
mvn spring-boot:run
# SPRING_PROFILES_ACTIVE=dev (default)

# Check logs - should see:
# 🔓 DEV MODE: Security disabled - All endpoints permit-all

# Test without auth
curl http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"1Y"}'

# Expected: 201 CREATED (no auth required)
```

### 2. Test Prod Mode (Secure)

```bash
# Start in prod mode
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run

# Check logs - should see:
# 🔐 PROD MODE: Security enabled - JWT required for /api/**

# Test health endpoint (public)
curl http://localhost:8001/actuator/health
# Expected: {"status":"UP"}

# Test API without auth (should fail)
curl http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"1Y"}'

# Expected: 401 Unauthorized

# Test API with JWT (should work)
curl http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"ticker":"AAPL","period":"1Y"}'

# Expected: 201 CREATED
```

### 3. Test CORS

```bash
# Dev mode - test preflight
curl -X OPTIONS http://localhost:8001/api/analysis/run \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -v

# Expected headers in response:
# Access-Control-Allow-Origin: http://localhost:3000
# Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
# Access-Control-Allow-Credentials: true
```

---

## JWT Configuration (Production)

### Auth0 Setup Example

1. **Create Auth0 Application:**
   - Go to Auth0 Dashboard → Applications → Create Application
   - Choose "Single Page Application"
   - Note the Domain and Client ID

2. **Set Environment Variables:**

```bash
# Production environment
export JWT_ISSUER_URI=https://your-tenant.auth0.com/
export JWT_JWK_SET_URI=https://your-tenant.auth0.com/.well-known/jwks.json
export CORS_ALLOWED_ORIGINS=https://app.marketlens.com
```

3. **Test JWT Validation:**

```bash
# Get JWT token from Auth0
# (use Auth0 SDK in your frontend)

# Call protected endpoint
curl http://localhost:8001/api/analysis/run \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"1Y"}'
```

---

## Troubleshooting

### BeanDefinitionOverrideException

**Problem:** Multiple `SecurityFilterChain` beans with the same name

**Solution:** ✅ Already fixed! Using unified `SecurityConfig` with:
- Single `@EnableWebSecurity`
- Different bean names: `devSecurityFilterChain` vs `prodSecurityFilterChain`
- Profile-specific `@Bean` annotations

### 401 Unauthorized in Dev Mode

**Problem:** API returns 401 even in dev mode

**Solution:**
1. Verify profile: Check logs for "🔓 DEV MODE" message
2. Check `SPRING_PROFILES_ACTIVE=dev` environment variable
3. Restart application after profile change

### CORS Errors in Browser

**Problem:** Browser shows "CORS policy blocked" error

**Solutions:**
1. Verify frontend origin is in `cors.allowed.origins`
2. Check browser sends `Origin` header in request
3. Verify CORS config logs show correct origins
4. For dev, ensure using `http://localhost:3000` not `http://127.0.0.1:3000` (or add both)

### JWT Validation Fails

**Problem:** Valid JWT returns 401 Unauthorized

**Solutions:**
1. Verify `JWT_ISSUER_URI` matches JWT `iss` claim
2. Check `JWT_JWK_SET_URI` is accessible from backend
3. Ensure JWT has not expired
4. Check JWT `aud` (audience) claim matches expected audience

### H2 Console Not Accessible

**Problem:** H2 console returns 403 Forbidden

**Solutions:**
1. Only works in dev profile (not prod)
2. Verify H2 dependency in pom.xml
3. Check `spring.h2.console.enabled=true` in application-dev.properties
4. Access at: http://localhost:8001/h2-console

---

## Summary

✅ **Unified Security Config** - Single `@EnableWebSecurity`, no bean conflicts
✅ **Profile-Specific Beans** - Dev vs Prod with different security rules
✅ **Dev: Fully Permissive** - No auth, CSRF disabled, H2 console enabled
✅ **Prod: Secure** - JWT required, security headers, public health checks
✅ **CORS Configured** - Environment-driven, shared across profiles
✅ **No Bean Override** - Clean design, no `allow-bean-definition-overriding` needed

**Your backend now has production-grade security without bean conflicts!** 🎉
