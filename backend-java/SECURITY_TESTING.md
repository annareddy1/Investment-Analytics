# Security Testing Guide

Quick reference for testing Spring Security OAuth2 implementation.

---

## Quick Setup for Testing

### Option 1: Mock JWT for Development (NO OAuth2 Provider Needed)

For local development/testing, you can temporarily bypass OAuth2 validation:

**Create `application-dev.properties`:**

```properties
# Disable OAuth2 for local testing (DEVELOPMENT ONLY!)
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
```

**Run with dev profile:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

⚠️ **WARNING:** Only use this for local development. NEVER in production!

---

### Option 2: Use jwt.io to Create Test Token

**1. Generate Test JWT at [jwt.io](https://jwt.io)**

**Header:**
```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "test-user-123",
  "email": "test@example.com",
  "scope": "USER ADMIN",
  "iss": "https://YOUR_DOMAIN.auth0.com/",
  "aud": "https://api.marketlens.com",
  "iat": 1700000000,
  "exp": 2000000000
}
```

**2. Sign with your OAuth2 provider's private key**

⚠️ **NOTE:** For this to work, the backend must be configured with the corresponding public key (JWK set).

---

## Testing Endpoints

### 1. Test Public Endpoint (No Auth Required)

```bash
curl http://localhost:8001/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

---

### 2. Test Protected Endpoint WITHOUT JWT (Should Fail)

```bash
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"1Y"}'
```

**Expected Response:** `401 Unauthorized`

```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/analysis/run"
}
```

---

### 3. Test Protected Endpoint WITH Valid JWT

```bash
# Replace YOUR_JWT_TOKEN with actual token
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8001/api/analysis/run \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"1Y"}'
```

**Expected Response:** `200 OK` or `201 Created`

```json
{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "AAPL",
  "status": "PROCESSING",
  "message": "Analysis job started",
  "progress": 0
}
```

---

### 4. Test User Profile Endpoint

```bash
curl http://localhost:8001/api/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "userId": "test-user-123",
  "email": "test@example.com",
  "username": "test@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"],
  "isAuthenticated": true,
  "allClaims": {
    "sub": "test-user-123",
    "email": "test@example.com",
    "scope": "USER ADMIN",
    "iss": "https://your-domain.auth0.com/",
    "exp": 2000000000,
    "iat": 1700000000
  }
}
```

---

### 5. Test Role-Based Access (ROLE_USER Required)

```bash
curl http://localhost:8001/api/user/user-only \
  -H "Authorization: Bearer $TOKEN"
```

**If JWT has ROLE_USER:** `200 OK`
```json
{
  "message": "This endpoint is accessible to ROLE_USER",
  "userId": "test-user-123"
}
```

**If JWT missing ROLE_USER:** `403 Forbidden`

---

### 6. Test Admin-Only Endpoint

```bash
curl http://localhost:8001/api/user/admin-only \
  -H "Authorization: Bearer $TOKEN"
```

**If JWT has ROLE_ADMIN:** `200 OK`
**If JWT missing ROLE_ADMIN:** `403 Forbidden`

```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

## Testing with Real OAuth2 Providers

### Auth0 Setup

**1. Create Auth0 Account:**
- Sign up at [auth0.com](https://auth0.com)

**2. Create API:**
- Dashboard → Applications → APIs → Create API
- Name: `MarketLens API`
- Identifier: `https://api.marketlens.com`
- Signing Algorithm: `RS256`

**3. Get Configuration:**
- Domain: `YOUR_DOMAIN.auth0.com`
- Issuer URI: `https://YOUR_DOMAIN.auth0.com/`
- JWK Set URI: `https://YOUR_DOMAIN.auth0.com/.well-known/jwks.json`

**4. Create Test User:**
- Dashboard → User Management → Users → Create User
- Email: `test@example.com`
- Password: `TestPassword123!`

**5. Assign Roles:**
- Dashboard → User Management → Roles → Create Role
- Name: `USER`
- Assign to test user

**6. Get Access Token (Machine-to-Machine):**

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

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

**7. Update application.properties:**

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_DOMAIN.auth0.com/
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_DOMAIN.auth0.com/.well-known/jwks.json
```

**8. Test:**

```bash
TOKEN="<access_token from step 6>"

curl http://localhost:8001/api/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

---

### Keycloak Setup (Self-Hosted)

**1. Run Keycloak with Docker:**

```bash
docker run -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:23.0 start-dev
```

**2. Access Admin Console:**
- URL: `http://localhost:8080`
- Username: `admin`
- Password: `admin`

**3. Create Realm:**
- Realm name: `marketlens`

**4. Create Client:**
- Client ID: `marketlens-backend`
- Client Protocol: `openid-connect`
- Access Type: `confidential`
- Valid Redirect URIs: `http://localhost:3000/*`

**5. Create User:**
- Username: `testuser`
- Email: `test@example.com`
- Set password in Credentials tab

**6. Create Roles:**
- Realm Roles → Create Role: `USER`
- Assign to test user

**7. Get Configuration:**
- Issuer URI: `http://localhost:8080/realms/marketlens`
- JWK Set URI: `http://localhost:8080/realms/marketlens/protocol/openid-connect/certs`

**8. Get Access Token:**

```bash
curl -X POST http://localhost:8080/realms/marketlens/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=marketlens-backend" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=testuser" \
  -d "password=TestPassword123!" \
  -d "grant_type=password"
```

**9. Update application.properties:**

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/marketlens
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/marketlens/protocol/openid-connect/certs
```

---

## Common Test Scenarios

### Scenario 1: Expired Token

**Test:**
```bash
# Use a token with "exp" in the past
curl http://localhost:8001/api/user/profile \
  -H "Authorization: Bearer $EXPIRED_TOKEN"
```

**Expected:** `401 Unauthorized`

**Fix:** Get a new token with valid expiration

---

### Scenario 2: Invalid Signature

**Test:**
```bash
# Modify a valid JWT (change any character)
curl http://localhost:8001/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.TAMPERED_PAYLOAD.signature"
```

**Expected:** `401 Unauthorized`

**Reason:** Signature verification failed

---

### Scenario 3: Missing Scope/Role

**Test:**
```bash
# Token without "USER" in scope claim
curl http://localhost:8001/api/user/user-only \
  -H "Authorization: Bearer $TOKEN_WITHOUT_USER_ROLE"
```

**Expected:** `403 Forbidden`

**Fix:** Ensure JWT includes required scope/role

---

### Scenario 4: CORS Preflight

**Test:**
```bash
# OPTIONS request (CORS preflight)
curl -X OPTIONS http://localhost:8001/api/analysis/run \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization,Content-Type" \
  -v
```

**Expected Response Headers:**
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type, Accept, X-Requested-With, Cache-Control
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

---

## Debugging

### Enable Spring Security Debug Logging

**application.properties:**
```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2=TRACE
```

**Check Logs For:**
- JWT validation steps
- Authority extraction
- Authorization decisions

### Common Log Messages

**Successful Authentication:**
```
DEBUG o.s.s.o.s.r.BearerTokenAuthenticationFilter : Found bearer token in request
DEBUG o.s.s.o.s.r.JwtAuthenticationProvider : Authenticated token
```

**Failed Authentication:**
```
DEBUG o.s.s.o.s.r.BearerTokenAuthenticationFilter : Did not process request since did not find bearer token
DEBUG o.s.s.w.a.Http403ForbiddenEntryPoint : Pre-authenticated entry point called. Rejecting access
```

---

## Integration Tests (Optional)

**Create Test Class:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    public void testProtectedEndpointWithUser() throws Exception {
        mockMvc.perform(post("/api/analysis/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticker\":\"AAPL\",\"period\":\"1Y\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void testProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/analysis/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticker\":\"AAPL\",\"period\":\"1Y\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPublicEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
}
```

---

## Checklist

Before deploying to production:

- [ ] OAuth2 provider configured (Auth0, Keycloak, etc.)
- [ ] `issuer-uri` and `jwk-set-uri` set in application.properties
- [ ] CORS origins restricted (not `*`)
- [ ] Frontend obtains and sends JWT in `Authorization: Bearer` header
- [ ] Public endpoints accessible without auth
- [ ] Protected endpoints return 401 without valid JWT
- [ ] Role-based access control working (403 for insufficient roles)
- [ ] Token expiration handled gracefully in frontend
- [ ] Logs reviewed for security warnings
- [ ] Integration tests passing
