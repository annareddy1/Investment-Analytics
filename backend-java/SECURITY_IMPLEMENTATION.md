# Spring Security 6 OAuth2 Implementation Guide

## Overview

MarketLens backend is secured with Spring Security 6 as an OAuth2 Resource Server, validating JWT tokens issued by an external OAuth2 Authorization Server (Auth0, Keycloak, AWS Cognito, etc.).

**Architecture:**
- **Frontend (React)** → Obtains JWT from OAuth2 provider → Sends JWT in `Authorization` header
- **Backend (Spring Boot)** → Validates JWT signature and claims → Extracts user info → Authorizes request

---

## Configuration Files

### 1. Maven Dependencies (`pom.xml`)

```xml
<!-- Spring Security 6 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Resource Server with JWT support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Spring Boot Actuator for health checks -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. Application Properties (`application.properties`)

**Required Configuration:**

```properties
# OAuth2 Resource Server - JWT Validation
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_AUTH_PROVIDER_DOMAIN
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_AUTH_PROVIDER_DOMAIN/.well-known/jwks.json
```

**Provider-Specific Examples:**

**Auth0:**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_DOMAIN.auth0.com/
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_DOMAIN.auth0.com/.well-known/jwks.json
```

**Keycloak:**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/YOUR_REALM
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/YOUR_REALM/protocol/openid-connect/certs
```

**AWS Cognito:**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json
```

**Azure AD:**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/YOUR_TENANT_ID/v2.0
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://login.microsoftonline.com/YOUR_TENANT_ID/discovery/v2.0/keys
```

---

## How JWT Validation Works

### JWT Validation Flow

1. **Frontend sends request:**
   ```
   GET /api/analysis/run
   Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwic2NvcGUiOiJST0xFX1VTRVIiLCJleHAiOjE3MDAwMDAwMDB9.signature
   ```

2. **Spring Security extracts JWT from Authorization header**

3. **JWT Validation Steps:**
   - **Signature Verification:** Downloads JWK (JSON Web Key) from `jwk-set-uri` and verifies JWT signature
   - **Issuer Validation:** Checks `iss` claim matches `issuer-uri`
   - **Expiration Check:** Verifies `exp` claim is in the future
   - **Not Before Check:** Verifies `nbf` claim (if present) is in the past
   - **Audience Validation:** Can be configured to check `aud` claim

4. **If valid:** Request proceeds with authenticated user context

5. **If invalid:** Returns `401 Unauthorized`

### JWT Claims Mapping

**Standard JWT Claims:**

| JWT Claim | Purpose | Spring Security Mapping |
|-----------|---------|-------------------------|
| `sub` | Subject (User ID) | `Authentication.getName()`, `Jwt.getSubject()` |
| `iss` | Issuer | Validated against `issuer-uri` |
| `exp` | Expiration timestamp | Validated automatically |
| `iat` | Issued at timestamp | Available in JWT claims |
| `scope` or `scp` | Scopes/authorities | Converted to `GrantedAuthority` with `ROLE_` prefix |
| `email` | User email | Custom claim, access via `Jwt.getClaim("email")` |
| `name` | User name | Custom claim |

**Example JWT Payload:**

```json
{
  "sub": "auth0|507f1f77bcf86cd799439011",
  "email": "user@example.com",
  "name": "John Doe",
  "scope": "USER ADMIN",
  "iss": "https://your-domain.auth0.com/",
  "aud": "https://api.marketlens.com",
  "iat": 1700000000,
  "exp": 1700086400
}
```

**Mapped Authorities:**
- `ROLE_USER`
- `ROLE_ADMIN`

---

## Security Configuration

### SecurityConfig.java

**Key Features:**

1. **Stateless Sessions:** No session cookies, every request validated independently
2. **CORS Enabled:** Configured for React frontend
3. **Public Endpoints:** `/actuator/health`, `/actuator/info` accessible without authentication
4. **Protected Endpoints:** All `/api/**` require valid JWT
5. **Role-Based Access:** Roles extracted from JWT `scope` claim

**Authorization Rules:**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health", "/actuator/info").permitAll()  // Public
    .requestMatchers("/api/**").authenticated()                          // Require JWT
    .anyRequest().authenticated()
)
```

---

## Using Authentication in Controllers

### Method 1: JwtUserExtractor Utility (Recommended)

```java
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final JwtUserExtractor jwtUserExtractor;
    private final AnalysisService analysisService;

    @PostMapping("/run")
    public ResponseEntity<?> runAnalysis(@RequestBody AnalysisRequest request) {
        // Extract authenticated user ID
        String userId = jwtUserExtractor.getUserId();

        log.info("User {} requested analysis for {}", userId, request.getTicker());

        // Use userId for auditing, ownership, etc.
        AnalysisRun analysis = analysisService.createAnalysis(
            request.getTicker(),
            request.getPeriod(),
            userId  // Store who created this analysis
        );

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/my-analyses")
    public ResponseEntity<?> getMyAnalyses() {
        String userId = jwtUserExtractor.getUserId();

        // Return only analyses owned by this user
        List<AnalysisRun> analyses = analysisService.findByUserId(userId);

        return ResponseEntity.ok(analyses);
    }
}
```

### Method 2: Using Principal Parameter

```java
@GetMapping("/profile")
public ResponseEntity<?> getProfile(Principal principal) {
    if (principal instanceof JwtAuthenticationToken jwtAuth) {
        String userId = jwtAuth.getToken().getSubject();
        String email = jwtAuth.getToken().getClaimAsString("email");

        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "email", email
        ));
    }
    return ResponseEntity.status(401).build();
}
```

### Method 3: Method-Level Security

```java
// Only users with ROLE_USER can access
@PreAuthorize("hasRole('USER')")
@PostMapping("/analysis/run")
public ResponseEntity<?> runAnalysis(@RequestBody AnalysisRequest request) {
    // Implementation
}

// Only ROLE_ADMIN can access
@Secured("ROLE_ADMIN")
@DeleteMapping("/analysis/{id}")
public ResponseEntity<?> deleteAnalysis(@PathVariable String id) {
    // Implementation
}

// Multiple roles allowed
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@GetMapping("/analysis/{id}")
public ResponseEntity<?> getAnalysis(@PathVariable String id) {
    // Implementation
}
```

---

## Frontend Integration

### React Example with Axios

**1. Obtain JWT from OAuth2 Provider**

```javascript
// Using Auth0 React SDK
import { useAuth0 } from '@auth0/auth0-react';

function App() {
  const { loginWithRedirect, logout, user, isAuthenticated, getAccessTokenSilently } = useAuth0();

  const callProtectedApi = async () => {
    try {
      // Get JWT access token
      const token = await getAccessTokenSilently();

      // Call protected endpoint
      const response = await axios.post(
        `${BACKEND_URL}/api/analysis/run`,
        { ticker: 'AAPL', period: '1Y' },
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      console.log('Analysis started:', response.data);
    } catch (error) {
      console.error('API call failed:', error);
    }
  };

  return (
    <div>
      {!isAuthenticated && <button onClick={loginWithRedirect}>Log In</button>}
      {isAuthenticated && (
        <>
          <p>Welcome {user.name}</p>
          <button onClick={callProtectedApi}>Run Analysis</button>
          <button onClick={logout}>Log Out</button>
        </>
      )}
    </div>
  );
}
```

**2. Axios Interceptor (Automatic JWT Attachment)**

```javascript
// api.js
import axios from 'axios';
import { auth0Client } from './auth0';

const api = axios.create({
  baseURL: process.env.REACT_APP_BACKEND_URL
});

// Request interceptor - automatically attach JWT to every request
api.interceptors.request.use(
  async (config) => {
    try {
      // Get fresh access token
      const token = await auth0Client.getTokenSilently();

      // Attach to Authorization header
      config.headers.Authorization = `Bearer ${token}`;
    } catch (error) {
      console.error('Failed to get access token:', error);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid - redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

**3. Using the API Client**

```javascript
// Usage in components
import api from './services/api';

// All requests automatically include JWT
const runAnalysis = async (ticker, period) => {
  const response = await api.post('/api/analysis/run', { ticker, period });
  return response.data;
};

const getMyProfile = async () => {
  const response = await api.get('/api/user/profile');
  return response.data;
};
```

### Plain JavaScript Fetch Example

```javascript
// Get JWT token (implementation depends on OAuth2 provider)
const token = getJwtToken();

// Make authenticated request
fetch('http://localhost:8001/api/analysis/run', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({ ticker: 'AAPL', period: '1Y' })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

---

## Testing with cURL

### 1. Obtain JWT Token

**From Auth0 (example):**

```bash
curl --request POST \
  --url 'https://YOUR_DOMAIN.auth0.com/oauth/token' \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"YOUR_CLIENT_ID",
    "client_secret":"YOUR_CLIENT_SECRET",
    "audience":"https://api.marketlens.com",
    "grant_type":"client_credentials"
  }'
```

Response:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

### 2. Test Protected Endpoint

```bash
# Store token in variable
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Call protected endpoint
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"1Y"}'
```

### 3. Test Public Endpoint

```bash
# No token required
curl http://localhost:8001/actuator/health
```

---

## Common Issues and Troubleshooting

### 401 Unauthorized

**Possible Causes:**

1. **Missing Authorization header:**
   ```
   Request headers must include: Authorization: Bearer <token>
   ```

2. **Invalid JWT signature:**
   - Verify `jwk-set-uri` is correct
   - JWT must be signed by the configured OAuth2 provider

3. **Expired token:**
   - Check `exp` claim in JWT
   - Frontend should refresh tokens before expiration

4. **Incorrect issuer:**
   - `iss` claim in JWT must match `issuer-uri` configuration

**Debug Steps:**

```bash
# Enable Spring Security debug logging
logging.level.org.springframework.security=DEBUG

# Check JWT claims (decode at https://jwt.io)
```

### 403 Forbidden

**Cause:** User authenticated but lacks required role

**Solution:**
- Verify JWT contains correct `scope` or `scp` claim
- Check role configuration in OAuth2 provider
- Ensure `@PreAuthorize` or `@Secured` role matches JWT roles

### CORS Errors

**Cause:** Frontend on different origin than backend

**Solution:**
- Verify `cors.allowed.origins` in `application.properties` includes frontend URL
- Check browser console for specific CORS error
- Ensure `Authorization` header is in `allowedHeaders`

---

## Security Best Practices

### 1. Environment-Specific Configuration

**Never commit secrets to Git:**

```properties
# Use environment variables
spring.security.oauth2.resourceserver.jwt.issuer-uri=${OAUTH2_ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${OAUTH2_JWK_SET_URI}
```

**Set in deployment:**
```bash
export OAUTH2_ISSUER_URI=https://your-domain.auth0.com/
export OAUTH2_JWK_SET_URI=https://your-domain.auth0.com/.well-known/jwks.json
```

### 2. Audience Validation (Recommended)

Add audience validation to prevent token reuse across different APIs:

```java
@Bean
public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder
        .withJwkSetUri(jwkSetUri)
        .build();

    OAuth2TokenValidator<Jwt> audienceValidator =
        new AudienceValidator("https://api.marketlens.com");

    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(
        withIssuer,
        audienceValidator
    );

    decoder.setJwtValidator(withAudience);
    return decoder;
}
```

### 3. Token Expiration

**Backend:** Validates `exp` claim automatically

**Frontend:** Refresh tokens before expiration
```javascript
// Auth0 example - automatically handles refresh
const token = await getAccessTokenSilently({
  audience: 'https://api.marketlens.com',
  scope: 'read:analysis write:analysis'
});
```

### 4. Rate Limiting

Consider adding rate limiting to prevent abuse:

```xml
<!-- Add Bucket4j for rate limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
```

### 5. Logging and Monitoring

```properties
# Log authentication events
logging.level.org.springframework.security=INFO
logging.level.org.springframework.security.oauth2=DEBUG

# Monitor with Actuator
management.endpoints.web.exposure.include=health,metrics,httptrace
```

---

## Complete Example: Secure Analysis Flow

### 1. User logs in via React frontend
```javascript
const { loginWithRedirect } = useAuth0();
loginWithRedirect();
```

### 2. OAuth2 provider returns JWT
```json
{
  "sub": "auth0|507f191e810c19729de860ea",
  "email": "user@example.com",
  "scope": "USER",
  "iss": "https://marketlens.auth0.com/",
  "exp": 1700086400
}
```

### 3. Frontend sends request with JWT
```javascript
const token = await getAccessTokenSilently();

const response = await axios.post(
  'http://localhost:8001/api/analysis/run',
  { ticker: 'AAPL', period: '1Y' },
  { headers: { Authorization: `Bearer ${token}` } }
);
```

### 4. Backend validates JWT and processes request
```java
@PostMapping("/run")
public ResponseEntity<?> runAnalysis(@RequestBody AnalysisRequest request) {
    String userId = jwtUserExtractor.getUserId(); // "auth0|507f191e810c19729de860ea"

    AnalysisRun analysis = analysisService.createAnalysis(
        request.getTicker(),
        request.getPeriod(),
        userId
    );

    return ResponseEntity.ok(analysis);
}
```

### 5. Backend returns response
```json
{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "AAPL",
  "status": "PROCESSING",
  "userId": "auth0|507f191e810c19729de860ea"
}
```

---

## Next Steps

1. **Choose OAuth2 Provider:**
   - Auth0 (recommended for quick setup)
   - AWS Cognito (AWS-native)
   - Keycloak (self-hosted)
   - Azure AD (Microsoft ecosystem)

2. **Configure Provider:**
   - Create application/client
   - Configure allowed callbacks and logout URLs
   - Define roles/scopes (e.g., `USER`, `ADMIN`)
   - Get `issuer-uri` and `jwk-set-uri`

3. **Update application.properties:**
   - Set `spring.security.oauth2.resourceserver.jwt.issuer-uri`
   - Set `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`

4. **Integrate Frontend:**
   - Install OAuth2 SDK (`@auth0/auth0-react`, `aws-amplify`, etc.)
   - Configure SDK with client credentials
   - Implement login/logout flows
   - Attach JWT to API requests

5. **Test:**
   - Test public endpoints (no auth)
   - Test protected endpoints with valid JWT
   - Test role-based access control
   - Test token expiration handling

---

## Additional Resources

- [Spring Security 6 Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [OAuth2 Resource Server Guide](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWT.io](https://jwt.io) - Decode and verify JWTs
- [Auth0 Spring Security Quickstart](https://auth0.com/docs/quickstart/backend/java-spring-security5)
- [Baeldung Spring Security Guides](https://www.baeldung.com/spring-security-oauth-jwt)
