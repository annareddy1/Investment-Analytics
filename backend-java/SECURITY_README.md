# MarketLens Security Implementation Summary

Production-grade Spring Security 6 with OAuth2 Resource Server (JWT validation).

---

## ✅ What Was Implemented

### 1. **Spring Security 6 Dependencies**
- `spring-boot-starter-security` - Core security framework
- `spring-boot-starter-oauth2-resource-server` - JWT validation
- `spring-boot-starter-actuator` - Health monitoring

**File:** [`pom.xml`](pom.xml)

### 2. **Security Configuration**
- Modern Spring Security 6 (no deprecated `WebSecurityConfigurerAdapter`)
- Stateless JWT authentication (no sessions)
- OAuth2 Resource Server with automatic JWT validation
- Role-based access control (RBAC)
- CORS configuration for React frontend

**File:** [`src/main/java/com/marketlens/config/SecurityConfig.java`](src/main/java/com/marketlens/config/SecurityConfig.java)

### 3. **JWT Configuration**
- JWT issuer URI
- JWK Set URI for signature verification
- Actuator health endpoints

**File:** [`src/main/resources/application.properties`](src/main/resources/application.properties)

### 4. **JWT User Extractor Utility**
- Extract user ID (`sub` claim)
- Extract email and username
- Check user roles
- Access custom JWT claims

**File:** [`src/main/java/com/marketlens/security/JwtUserExtractor.java`](src/main/java/com/marketlens/security/JwtUserExtractor.java)

### 5. **Example Protected Controller**
- Demonstrates JWT authentication
- Shows role-based authorization
- Multiple methods to access user info

**File:** [`src/main/java/com/marketlens/controller/UserProfileController.java`](src/main/java/com/marketlens/controller/UserProfileController.java)

### 6. **Comprehensive Documentation**
- Full implementation guide
- Frontend integration examples
- OAuth2 provider setup instructions
- JWT validation explained

**File:** [`SECURITY_IMPLEMENTATION.md`](SECURITY_IMPLEMENTATION.md)

### 7. **Testing Guide**
- Quick testing with cURL
- OAuth2 provider setup (Auth0, Keycloak)
- Common scenarios and debugging

**File:** [`SECURITY_TESTING.md`](SECURITY_TESTING.md)

---

## 🔒 Security Features

| Feature | Status | Details |
|---------|--------|---------|
| **JWT Validation** | ✅ Enabled | Signature, issuer, expiration checked |
| **Stateless Sessions** | ✅ Enabled | No server-side sessions or cookies |
| **CORS** | ✅ Configured | Allows React frontend origins |
| **Role-Based Access** | ✅ Implemented | `ROLE_USER`, `ROLE_ADMIN` |
| **Public Endpoints** | ✅ Configured | `/actuator/health`, `/actuator/info` |
| **Protected Endpoints** | ✅ Secured | All `/api/**` require valid JWT |
| **User Extraction** | ✅ Utility Created | `JwtUserExtractor` component |

---

## 📋 Authorization Rules

| Endpoint Pattern | Access Level | Notes |
|-----------------|--------------|-------|
| `/actuator/health` | Public | No authentication required |
| `/actuator/info` | Public | No authentication required |
| `/api/**` | Authenticated | Valid JWT required |
| `/api/user/user-only` | `ROLE_USER` | Requires USER role in JWT |
| `/api/user/admin-only` | `ROLE_ADMIN` | Requires ADMIN role in JWT |

---

## 🚀 Quick Start

### 1. Configure OAuth2 Provider

**Update `application.properties`:**

```properties
# Replace with your OAuth2 provider details
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_DOMAIN.auth0.com/
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_DOMAIN.auth0.com/.well-known/jwks.json
```

**Supported Providers:**
- Auth0
- Keycloak
- AWS Cognito
- Azure AD
- Google OAuth2

### 2. Build and Run

```bash
cd backend-java
mvn clean install
mvn spring-boot:run
```

### 3. Test Public Endpoint

```bash
curl http://localhost:8001/actuator/health
# Should return: {"status":"UP"}
```

### 4. Test Protected Endpoint (Without Auth)

```bash
curl http://localhost:8001/api/user/profile
# Should return: 401 Unauthorized
```

### 5. Test Protected Endpoint (With JWT)

```bash
TOKEN="your_jwt_token_here"

curl http://localhost:8001/api/user/profile \
  -H "Authorization: Bearer $TOKEN"

# Should return user profile with JWT claims
```

---

## 🔧 Using Authentication in Controllers

### Example 1: Extract User ID

```java
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final JwtUserExtractor jwtUserExtractor;
    private final AnalysisService analysisService;

    @PostMapping("/run")
    public ResponseEntity<?> runAnalysis(@RequestBody AnalysisRequest request) {
        // Get authenticated user ID
        String userId = jwtUserExtractor.getUserId();

        log.info("User {} is running analysis for {}", userId, request.getTicker());

        // Create analysis with user ownership
        AnalysisRun analysis = analysisService.createAnalysis(
            request.getTicker(),
            request.getPeriod(),
            userId
        );

        return ResponseEntity.ok(analysis);
    }
}
```

### Example 2: Role-Based Access

```java
// Only ROLE_USER can access
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
```

---

## 🌐 Frontend Integration

### React + Auth0 Example

```javascript
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';

function Dashboard() {
  const { getAccessTokenSilently } = useAuth0();

  const runAnalysis = async () => {
    // Get JWT from Auth0
    const token = await getAccessTokenSilently();

    // Call protected API
    const response = await axios.post(
      'http://localhost:8001/api/analysis/run',
      { ticker: 'AAPL', period: '1Y' },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );

    console.log('Analysis started:', response.data);
  };

  return <button onClick={runAnalysis}>Run Analysis</button>;
}
```

### Axios Interceptor (Auto-attach JWT)

```javascript
// api.js
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_BACKEND_URL
});

// Automatically attach JWT to every request
api.interceptors.request.use(async (config) => {
  const token = await getAccessTokenSilently();
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default api;
```

---

## 🧪 Testing

### Test Public Endpoint
```bash
curl http://localhost:8001/actuator/health
# ✅ Should return: 200 OK
```

### Test Protected Endpoint (No Auth)
```bash
curl http://localhost:8001/api/user/profile
# ❌ Should return: 401 Unauthorized
```

### Test Protected Endpoint (With JWT)
```bash
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

curl http://localhost:8001/api/user/profile \
  -H "Authorization: Bearer $TOKEN"
# ✅ Should return: 200 OK with user profile
```

### Test Role-Based Access
```bash
# User with ROLE_USER
curl http://localhost:8001/api/user/user-only \
  -H "Authorization: Bearer $USER_TOKEN"
# ✅ Should return: 200 OK

# User without ROLE_ADMIN
curl http://localhost:8001/api/user/admin-only \
  -H "Authorization: Bearer $USER_TOKEN"
# ❌ Should return: 403 Forbidden
```

See [`SECURITY_TESTING.md`](SECURITY_TESTING.md) for detailed testing instructions.

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [`SECURITY_IMPLEMENTATION.md`](SECURITY_IMPLEMENTATION.md) | Complete implementation guide with examples |
| [`SECURITY_TESTING.md`](SECURITY_TESTING.md) | Testing guide with cURL commands |
| [`SecurityConfig.java`](src/main/java/com/marketlens/config/SecurityConfig.java) | Main security configuration |
| [`JwtUserExtractor.java`](src/main/java/com/marketlens/security/JwtUserExtractor.java) | Utility to extract JWT claims |
| [`UserProfileController.java`](src/main/java/com/marketlens/controller/UserProfileController.java) | Example protected controller |

---

## 🔍 How JWT Validation Works

1. **Frontend obtains JWT** from OAuth2 provider (Auth0, Keycloak, etc.)
2. **Frontend sends request** with `Authorization: Bearer <JWT>` header
3. **Spring Security intercepts** request and extracts JWT
4. **JWT Validation:**
   - ✅ Downloads public key from JWK Set URI
   - ✅ Verifies JWT signature
   - ✅ Checks issuer matches `issuer-uri`
   - ✅ Validates expiration (`exp` claim)
   - ✅ Extracts roles/scopes from JWT
5. **If valid:** Request proceeds with authenticated user context
6. **If invalid:** Returns `401 Unauthorized`

---

## ⚠️ Important Configuration

### Before Production Deployment:

1. **Set OAuth2 Provider URIs:**
   ```properties
   spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_PROVIDER
   spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_PROVIDER/.well-known/jwks.json
   ```

2. **Restrict CORS Origins:**
   ```properties
   cors.allowed.origins=https://yourdomain.com,https://app.yourdomain.com
   ```
   (Remove `localhost` and wildcards)

3. **Use Environment Variables:**
   ```bash
   export OAUTH2_ISSUER_URI=https://your-domain.auth0.com/
   export OAUTH2_JWK_SET_URI=https://your-domain.auth0.com/.well-known/jwks.json
   ```

4. **Enable Production Logging:**
   ```properties
   logging.level.org.springframework.security=INFO
   ```

---

## 🛡️ Security Best Practices

✅ **Implemented:**
- Stateless JWT authentication
- Signature verification via JWK Set
- Automatic expiration validation
- Role-based authorization
- CORS restricted to allowed origins
- No sessions or cookies
- HTTPS recommended (configure in production)

🔜 **Recommended Enhancements:**
- Audience validation (see `SECURITY_IMPLEMENTATION.md`)
- Rate limiting (Bucket4j)
- Request logging and monitoring
- API documentation (Springdoc OpenAPI)
- Integration tests
- Token refresh handling in frontend

---

## 🚨 Troubleshooting

### 401 Unauthorized
- ❌ Missing `Authorization` header
- ❌ Invalid JWT signature
- ❌ Expired token
- ❌ Wrong `issuer-uri` configuration

**Fix:** Verify JWT at [jwt.io](https://jwt.io) and check configuration

### 403 Forbidden
- ❌ User authenticated but lacks required role
- ❌ JWT missing `scope` or role claims

**Fix:** Check JWT contains correct roles (e.g., `"scope": "USER"`)

### CORS Errors
- ❌ Frontend origin not in `cors.allowed.origins`
- ❌ `Authorization` header not in `allowedHeaders`

**Fix:** Add frontend URL to allowed origins in `application.properties`

---

## 📖 Additional Resources

- [Spring Security 6 Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [OAuth2 Resource Server Guide](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWT.io](https://jwt.io) - Decode and verify JWTs
- [Auth0 Documentation](https://auth0.com/docs)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

---

## 📝 Summary

✅ **Spring Security 6** configured with modern approach (no deprecated classes)
✅ **OAuth2 Resource Server** validates JWTs from external provider
✅ **Stateless** - No server-side sessions
✅ **Role-Based Access Control** - `ROLE_USER`, `ROLE_ADMIN`
✅ **CORS** configured for React frontend
✅ **Public endpoints** - Health checks accessible without auth
✅ **User extraction utility** - Easy access to JWT claims in controllers
✅ **Production-ready** - Comprehensive security configuration
✅ **Well-documented** - Detailed guides and examples

**Next Step:** Configure your OAuth2 provider and update `application.properties` with the issuer and JWK Set URIs.
