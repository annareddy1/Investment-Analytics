# Auth0 Setup Guide for MarketLens

Complete step-by-step guide to configure Auth0 for MarketLens backend and frontend.

---

## Prerequisites

- MarketLens backend running (Spring Boot on port 8001)
- MarketLens frontend (React, will run on port 3000)
- Email address for Auth0 account

---

## Part 1: Auth0 Account Setup

### Step 1: Create Auth0 Account

1. Go to [auth0.com](https://auth0.com)
2. Click **Sign Up**
3. Choose signup method (email, Google, GitHub, etc.)
4. Complete registration
5. Create a **tenant** (e.g., `marketlens` or `marketlens-dev`)
   - **Region:** Choose closest to your users (US, EU, AU, etc.)
   - **Environment:** Development (for testing) or Production

**Your Auth0 Domain:** `YOUR_TENANT.auth0.com` or `YOUR_TENANT.us.auth0.com`

---

## Part 2: Create Auth0 API (Backend)

### Step 2: Register MarketLens API

This represents your Spring Boot backend.

1. **Navigate:** Auth0 Dashboard → Applications → APIs
2. **Click:** Create API
3. **Configure:**
   ```
   Name: MarketLens API
   Identifier: https://api.marketlens.com
   Signing Algorithm: RS256 (default)
   ```

   ⚠️ **Important:** The `Identifier` is the **Audience** (`aud` claim in JWT). Use a URI format, but it doesn't need to be a real URL.

4. **Click:** Create

### Step 3: Configure API Settings

1. **Navigate:** APIs → MarketLens API → Settings
2. **Note these values** (you'll need them later):
   ```
   Identifier: https://api.marketlens.com
   ```
3. **Scroll to:** RBAC Settings
   - ✅ Enable RBAC
   - ✅ Add Permissions in the Access Token
4. **Click:** Save

### Step 4: Define Permissions (Scopes)

1. **Navigate:** APIs → MarketLens API → Permissions
2. **Add Permissions:**

   | Permission | Description |
   |------------|-------------|
   | `read:analysis` | Read stock analysis data |
   | `write:analysis` | Create new stock analysis |
   | `delete:analysis` | Delete stock analysis |
   | `admin:all` | Full admin access |

   **Example:**
   ```
   Permission: read:analysis
   Description: Read stock analysis data
   ```

3. **Click:** Add for each permission

---

## Part 3: Create Auth0 Application (Frontend)

### Step 5: Register React Application

1. **Navigate:** Auth0 Dashboard → Applications → Applications
2. **Click:** Create Application
3. **Configure:**
   ```
   Name: MarketLens Frontend
   Type: Single Page Application (SPA)
   ```
4. **Click:** Create

### Step 6: Configure Application Settings

1. **Navigate:** Applications → MarketLens Frontend → Settings
2. **Note these values:**
   ```
   Domain: YOUR_TENANT.auth0.com
   Client ID: <copy this value>
   ```

3. **Configure URLs:**

   **Allowed Callback URLs:**
   ```
   http://localhost:3000,
   http://localhost:3000/callback,
   https://your-production-domain.com,
   https://your-production-domain.com/callback
   ```

   **Allowed Logout URLs:**
   ```
   http://localhost:3000,
   https://your-production-domain.com
   ```

   **Allowed Web Origins:**
   ```
   http://localhost:3000,
   https://your-production-domain.com
   ```

   **Allowed Origins (CORS):**
   ```
   http://localhost:3000,
   https://your-production-domain.com
   ```

4. **Scroll to:** Advanced Settings → Grant Types
   - ✅ Implicit
   - ✅ Authorization Code
   - ✅ Refresh Token

5. **Click:** Save Changes

---

## Part 4: Create Roles and Users

### Step 7: Create Roles

1. **Navigate:** User Management → Roles
2. **Click:** Create Role

**Create USER Role:**
```
Name: USER
Description: Standard user with read/write access to their own analyses
```

**Permissions:**
- ✅ `read:analysis`
- ✅ `write:analysis`

**Create ADMIN Role:**
```
Name: ADMIN
Description: Administrator with full access
```

**Permissions:**
- ✅ `read:analysis`
- ✅ `write:analysis`
- ✅ `delete:analysis`
- ✅ `admin:all`

### Step 8: Create Test User

1. **Navigate:** User Management → Users
2. **Click:** Create User
3. **Configure:**
   ```
   Email: test@example.com
   Password: TestPassword123!
   Connection: Username-Password-Authentication
   ```
4. **Click:** Create

### Step 9: Assign Role to User

1. **Navigate:** User Management → Users → test@example.com
2. **Click:** Roles tab
3. **Click:** Assign Roles
4. **Select:** USER
5. **Click:** Assign

---

## Part 5: Configure Custom Claims (Add Roles to JWT)

Auth0 doesn't include roles in the JWT by default. We need to add them using an Action.

### Step 10: Create Auth0 Action

1. **Navigate:** Actions → Flows → Login
2. **Click:** + (Add Action) → Build Custom
3. **Configure:**
   ```
   Name: Add Roles to Access Token
   Trigger: Login / Post Login
   Runtime: Node 18
   ```

4. **Replace code with:**

```javascript
/**
* Handler that will be called during the execution of a PostLogin flow.
*
* @param {Event} event - Details about the user and the context in which they are logging in.
* @param {PostLoginAPI} api - Interface whose methods can be used to change the behavior of the login.
*/
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://api.marketlens.com';

  // Add roles to access token
  if (event.authorization) {
    const roles = event.authorization.roles || [];

    // Convert roles to match Spring Security format
    const springRoles = roles.map(role => `ROLE_${role}`);

    // Add as 'scope' claim for Spring Security to recognize
    api.accessToken.setCustomClaim(`${namespace}/roles`, springRoles);

    // Also add roles array for frontend use
    api.accessToken.setCustomClaim(`${namespace}/permissions`, event.authorization.roles);
  }

  // Add user metadata
  api.accessToken.setCustomClaim(`${namespace}/email`, event.user.email);
  api.accessToken.setCustomClaim(`${namespace}/name`, event.user.name);
};
```

5. **Click:** Deploy

6. **Drag the Action** from the right panel into the Login flow (between Start and Complete)

7. **Click:** Apply

---

## Part 6: Configure Spring Boot Backend

### Step 11: Update application.properties

**File:** `backend-java/src/main/resources/application.properties`

```properties
# OAuth2 Resource Server (JWT) Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_TENANT.auth0.com/
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_TENANT.auth0.com/.well-known/jwks.json

# Example for US region tenant:
# spring.security.oauth2.resourceserver.jwt.issuer-uri=https://marketlens.us.auth0.com/
# spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://marketlens.us.auth0.com/.well-known/jwks.json
```

**Replace:** `YOUR_TENANT` with your actual Auth0 tenant name

### Step 12: Update SecurityConfig for Auth0 Roles

Auth0 stores roles in a custom claim, not the standard `scope` claim. Update the JWT converter:

**File:** `backend-java/src/main/java/com/marketlens/config/SecurityConfig.java`

Update the `jwtAuthenticationConverter()` method:

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    // Auth0 specific: Read roles from custom claim
    grantedAuthoritiesConverter.setAuthoritiesClaimName("https://api.marketlens.com/roles");
    grantedAuthoritiesConverter.setAuthorityPrefix(""); // Roles already have ROLE_ prefix from Action

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

    return jwtAuthenticationConverter;
}
```

### Step 13: Build and Run Backend

```bash
cd backend-java
mvn clean install
mvn spring-boot:run
```

**Verify startup logs:**
```
INFO  o.s.s.config.annotation.web.builders.WebSecurity : Will secure any request with [...]
```

---

## Part 7: Test Backend with Auth0

### Step 14: Get Access Token

**Method 1: Using cURL (Machine-to-Machine)**

First, create a Machine-to-Machine application in Auth0:

1. **Navigate:** Applications → Applications → Create Application
2. **Select:** Machine to Machine Applications
3. **Name:** MarketLens Testing
4. **Authorize:** MarketLens API
5. **Permissions:** Select all
6. **Copy:** Client ID and Client Secret

**Get token:**
```bash
curl --request POST \
  --url 'https://YOUR_TENANT.auth0.com/oauth/token' \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"YOUR_M2M_CLIENT_ID",
    "client_secret":"YOUR_M2M_CLIENT_SECRET",
    "audience":"https://api.marketlens.com",
    "grant_type":"client_credentials"
  }'
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

**Method 2: Using Auth0 Testing Tool**

1. **Navigate:** Applications → APIs → MarketLens API → Test
2. **Copy** the generated token

### Step 15: Test Protected Endpoint

```bash
# Set token
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Test user profile endpoint
curl http://localhost:8001/api/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "userId": "auth0|507f191e810c19729de860ea",
  "email": "test@example.com",
  "username": "test@example.com",
  "roles": ["ROLE_USER"],
  "isAuthenticated": true
}
```

### Step 16: Verify JWT Claims

Decode your token at [jwt.io](https://jwt.io)

**Expected claims:**
```json
{
  "iss": "https://YOUR_TENANT.auth0.com/",
  "sub": "auth0|507f191e810c19729de860ea",
  "aud": "https://api.marketlens.com",
  "iat": 1700000000,
  "exp": 1700086400,
  "https://api.marketlens.com/roles": ["ROLE_USER"],
  "https://api.marketlens.com/email": "test@example.com",
  "https://api.marketlens.com/name": "Test User"
}
```

---

## Part 8: Configure React Frontend

### Step 17: Install Auth0 React SDK

```bash
cd frontend
npm install @auth0/auth0-react
```

### Step 18: Create Auth0 Config

**Create file:** `frontend/src/auth0-config.js`

```javascript
export const auth0Config = {
  domain: "YOUR_TENANT.auth0.com",
  clientId: "YOUR_CLIENT_ID",
  authorizationParams: {
    redirect_uri: window.location.origin,
    audience: "https://api.marketlens.com",
    scope: "openid profile email read:analysis write:analysis"
  }
};
```

**Replace:**
- `YOUR_TENANT.auth0.com` with your Auth0 domain
- `YOUR_CLIENT_ID` with your SPA Client ID

### Step 19: Wrap App with Auth0Provider

**Update file:** `frontend/src/index.js`

```javascript
import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import App from './App';
import { auth0Config } from './auth0-config';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <Auth0Provider
      domain={auth0Config.domain}
      clientId={auth0Config.clientId}
      authorizationParams={auth0Config.authorizationParams}
    >
      <App />
    </Auth0Provider>
  </React.StrictMode>
);
```

### Step 20: Create Auth Components

**Create file:** `frontend/src/components/LoginButton.jsx`

```javascript
import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const LoginButton = () => {
  const { loginWithRedirect } = useAuth0();

  return (
    <button
      onClick={() => loginWithRedirect()}
      className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
    >
      Log In
    </button>
  );
};

export default LoginButton;
```

**Create file:** `frontend/src/components/LogoutButton.jsx`

```javascript
import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const LogoutButton = () => {
  const { logout } = useAuth0();

  return (
    <button
      onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
      className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
    >
      Log Out
    </button>
  );
};

export default LogoutButton;
```

**Create file:** `frontend/src/components/Profile.jsx`

```javascript
import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const Profile = () => {
  const { user, isAuthenticated, isLoading } = useAuth0();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="p-4 bg-gray-100 rounded">
      <img src={user.picture} alt={user.name} className="w-12 h-12 rounded-full" />
      <h2 className="text-xl font-bold">{user.name}</h2>
      <p className="text-gray-600">{user.email}</p>
    </div>
  );
};

export default Profile;
```

### Step 21: Create Protected API Client

**Create file:** `frontend/src/services/api.js`

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_BACKEND_URL || 'http://localhost:8001'
});

// Function to set auth token
export const setAuthToken = (token) => {
  if (token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common['Authorization'];
  }
};

// API methods
export const runAnalysis = async (ticker, period) => {
  const response = await api.post('/api/analysis/run', { ticker, period });
  return response.data;
};

export const getAnalysis = async (analysisId) => {
  const response = await api.get(`/api/analysis/${analysisId}`);
  return response.data;
};

export const getUserProfile = async () => {
  const response = await api.get('/api/user/profile');
  return response.data;
};

export default api;
```

### Step 22: Update App Component

**Update file:** `frontend/src/App.js`

```javascript
import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import LoginButton from './components/LoginButton';
import LogoutButton from './components/LogoutButton';
import Profile from './components/Profile';
import { setAuthToken, runAnalysis, getUserProfile } from './services/api';

function App() {
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    const initAuth = async () => {
      if (isAuthenticated) {
        try {
          // Get access token
          const token = await getAccessTokenSilently({
            authorizationParams: {
              audience: 'https://api.marketlens.com',
              scope: 'openid profile email read:analysis write:analysis'
            }
          });

          // Set token for all API requests
          setAuthToken(token);

          // Fetch user profile from backend
          const userProfile = await getUserProfile();
          setProfile(userProfile);
        } catch (error) {
          console.error('Error getting token:', error);
        }
      }
    };

    initAuth();
  }, [isAuthenticated, getAccessTokenSilently]);

  const handleRunAnalysis = async () => {
    try {
      const result = await runAnalysis('AAPL', '1Y');
      console.log('Analysis started:', result);
      alert(`Analysis started! ID: ${result.analysisId}`);
    } catch (error) {
      console.error('Error running analysis:', error);
      alert('Failed to run analysis. Check console for details.');
    }
  };

  if (isLoading) {
    return <div className="flex items-center justify-center h-screen">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-8">MarketLens</h1>

        <div className="bg-white rounded-lg shadow p-6 mb-6">
          {!isAuthenticated ? (
            <div className="text-center">
              <h2 className="text-2xl mb-4">Please log in to access MarketLens</h2>
              <LoginButton />
            </div>
          ) : (
            <div>
              <div className="flex justify-between items-center mb-6">
                <Profile />
                <LogoutButton />
              </div>

              {profile && (
                <div className="mb-6 p-4 bg-blue-50 rounded">
                  <h3 className="font-bold mb-2">Backend Profile:</h3>
                  <pre className="text-sm overflow-auto">
                    {JSON.stringify(profile, null, 2)}
                  </pre>
                </div>
              )}

              <div className="text-center">
                <button
                  onClick={handleRunAnalysis}
                  className="px-6 py-3 bg-green-500 text-white rounded-lg hover:bg-green-600 text-lg"
                >
                  Run Analysis for AAPL
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
```

### Step 23: Update Environment Variables

**Create/Update file:** `frontend/.env.local`

```env
REACT_APP_BACKEND_URL=http://localhost:8001
```

### Step 24: Run Frontend

```bash
cd frontend
npm start
```

Visit: `http://localhost:3000`

---

## Part 9: Test Complete Flow

### Step 25: End-to-End Test

1. **Open:** `http://localhost:3000`
2. **Click:** "Log In"
3. **Auth0 Login:** Enter credentials (`test@example.com` / `TestPassword123!`)
4. **Redirect:** Back to app, authenticated
5. **See:** User profile from backend
6. **Click:** "Run Analysis for AAPL"
7. **Verify:** Analysis started successfully

### Step 26: Check Backend Logs

Backend should show:
```
INFO  c.m.controller.AnalysisController : User auth0|507f191e810c19729de860ea requested analysis for AAPL
```

---

## Troubleshooting

### Issue: 401 Unauthorized

**Check:**
1. Token is being sent: Open DevTools → Network → Request Headers → `Authorization: Bearer ...`
2. Backend `issuer-uri` matches Auth0 domain exactly (including trailing `/`)
3. Backend is running and accessible

**Debug:**
```bash
# Enable Spring Security debug logging
logging.level.org.springframework.security=DEBUG
```

### Issue: 403 Forbidden

**Check:**
1. JWT contains roles claim: Decode at [jwt.io](https://jwt.io)
2. Auth0 Action is deployed and added to Login flow
3. User has been assigned a role in Auth0

### Issue: CORS Error

**Check:**
1. `cors.allowed.origins` in `application.properties` includes `http://localhost:3000`
2. Frontend origin is in Auth0 Allowed Web Origins

### Issue: "Audience is invalid"

**Check:**
1. Frontend requests token with correct audience:
   ```javascript
   audience: 'https://api.marketlens.com'
   ```
2. Backend `application.properties` doesn't have audience validation configured (or matches)

---

## Configuration Summary

### Auth0 Settings

| Setting | Value |
|---------|-------|
| **Tenant** | YOUR_TENANT.auth0.com |
| **API Name** | MarketLens API |
| **API Identifier** | https://api.marketlens.com |
| **SPA Client ID** | (from Auth0 dashboard) |
| **Roles** | USER, ADMIN |
| **Permissions** | read:analysis, write:analysis, delete:analysis, admin:all |

### Backend Settings

**File:** `application.properties`
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_TENANT.auth0.com/
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_TENANT.auth0.com/.well-known/jwks.json
```

### Frontend Settings

**File:** `frontend/src/auth0-config.js`
```javascript
{
  domain: "YOUR_TENANT.auth0.com",
  clientId: "YOUR_CLIENT_ID",
  authorizationParams: {
    audience: "https://api.marketlens.com",
    scope: "openid profile email read:analysis write:analysis"
  }
}
```

---

## Next Steps

1. **Production Deployment:**
   - Update Allowed Callback URLs with production domain
   - Update CORS origins in backend
   - Use environment variables for secrets

2. **Enhanced Security:**
   - Enable MFA in Auth0
   - Configure email verification
   - Set up rate limiting
   - Add audience validation in backend

3. **User Management:**
   - Customize Auth0 login page with your branding
   - Set up email templates
   - Configure social logins (Google, GitHub, etc.)

---

## Useful Auth0 URLs

- **Dashboard:** https://manage.auth0.com/
- **Documentation:** https://auth0.com/docs
- **React Quickstart:** https://auth0.com/docs/quickstart/spa/react
- **Spring Security Guide:** https://auth0.com/docs/quickstart/backend/java-spring-security5

---

## Complete! 🎉

Your MarketLens application is now secured with Auth0:
- ✅ JWT-based authentication
- ✅ Role-based authorization
- ✅ Stateless architecture
- ✅ Production-ready security
