# ✅ Spring Boot Actuator Configuration

## Overview

Spring Boot Actuator is now configured with minimal safe endpoints for health monitoring and application information. DB connectivity (PostgreSQL + MongoDB) is automatically included in health checks.

---

## Exposed Endpoints

### `/actuator/health` - Health Check
**Purpose:** Hosting platform health checks (AWS, GCP, Kubernetes, Docker)

**Dev Response (unauthenticated):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500068036608,
        "free": 348903604224,
        "threshold": 10485760,
        "exists": true
      }
    },
    "mongo": {
      "status": "UP",
      "details": {
        "version": "7.0.4"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Prod Response (unauthenticated):**
```json
{
  "status": "UP"
}
```

**Prod Response (authenticated with JWT):**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "mongo": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

### `/actuator/info` - Application Information
**Purpose:** Version, build info, environment details

**Response:**
```json
{
  "app": {
    "name": "marketlens-backend",
    "version": "1.0.0"
  },
  "java": {
    "version": "17.0.8",
    "vendor": "Eclipse Adoptium"
  },
  "os": {
    "name": "Linux",
    "version": "5.15.0",
    "arch": "amd64"
  }
}
```

---

## Configuration Files

### 1. **application.properties** (Base)
```properties
# Expose only safe endpoints
management.endpoints.web.exposure.include=health,info

# Base path
management.endpoints.web.base-path=/actuator

# Health shows components (db, mongo, diskSpace)
management.endpoint.health.show-components=always

# Info endpoint includes env, java, os
management.info.env.enabled=true
management.info.java.enabled=true
management.info.os.enabled=true
```

### 2. **application-dev.properties** (Development)
```properties
# Show full health details without authentication
management.endpoint.health.show-details=always

# JVM metrics
management.metrics.enable.jvm=true
```

### 3. **application-prod.properties** (Production)
```properties
# Show health details only when authenticated
management.endpoint.health.show-details=when-authorized

# Kubernetes/Docker health probes
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

---

## Security Configuration

### Dev Profile (DevSecurityConfig.java)
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/**").permitAll()  // All actuator endpoints allowed in dev
    .requestMatchers("/api/**").permitAll()
    .anyRequest().permitAll()
)
```

**Result:** All actuator endpoints accessible without authentication in dev

### Prod Profile (SecurityConfig.java)
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health", "/actuator/info").permitAll()  // Public for health checks
    .requestMatchers("/api/**").authenticated()  // API requires JWT
    .anyRequest().authenticated()
)
```

**Result:** `/actuator/health` and `/actuator/info` publicly accessible in prod (for hosting platform health checks)

---

## Database Health Checks

### Automatic Health Indicators

Spring Boot Actuator automatically detects and configures health indicators for:

1. **PostgreSQL (DataSource)**
   - Checks if database connection is valid
   - Uses `SELECT 1` validation query (configured in application-prod.properties)
   - Component name: `db`

2. **MongoDB**
   - Checks if MongoDB connection is alive
   - Runs `ping` command
   - Component name: `mongo`

**No additional configuration needed!** Just having `spring-boot-starter-data-jpa` and `spring-boot-starter-data-mongodb` in your pom.xml is enough.

---

## Testing

### 1. Test in Dev Mode

**Start backend:**
```bash
cd backend-java
mvn spring-boot:run
# Profile: dev (default)
```

**Test health endpoint:**
```bash
curl http://localhost:8001/actuator/health | jq
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "mongo": { "status": "UP", "details": { "version": "7.0.4" } },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

**Test info endpoint:**
```bash
curl http://localhost:8001/actuator/info | jq
```

**Test with databases down:**
```bash
# Stop PostgreSQL
docker-compose stop postgres

# Health should show DOWN
curl http://localhost:8001/actuator/health | jq

# Expected:
{
  "status": "DOWN",
  "components": {
    "db": { "status": "DOWN", "details": { "error": "Connection refused" } },
    "mongo": { "status": "UP" }
  }
}
```

---

### 2. Test in Prod Mode

**Start backend:**
```bash
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

**Test unauthenticated health endpoint:**
```bash
curl http://localhost:8001/actuator/health | jq
```

**Expected Response (minimal):**
```json
{
  "status": "UP"
}
```

**Test authenticated health endpoint (with JWT):**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8001/actuator/health | jq
```

**Expected Response (full details):**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "mongo": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

## Kubernetes/Docker Health Probes

In production, you can use these endpoints for container health checks:

### Kubernetes Example

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: marketlens-backend
spec:
  containers:
  - name: backend
    image: marketlens-backend:latest
    ports:
    - containerPort: 8001
    livenessProbe:
      httpGet:
        path: /actuator/health/liveness
        port: 8001
      initialDelaySeconds: 30
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness
        port: 8001
      initialDelaySeconds: 30
      periodSeconds: 10
```

### Docker Compose Example

```yaml
services:
  backend:
    image: marketlens-backend:latest
    ports:
      - "8001:8001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8001/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 40s
```

---

## Custom Health Indicators (Optional)

If you want to add custom health checks (e.g., external API availability), create a custom health indicator:

**Example: Yahoo Finance API Health Check**

```java
package com.marketlens.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class YahooFinanceHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/AAPL";
            restTemplate.getForObject(url, String.class);
            return Health.up().withDetail("api", "Yahoo Finance API is reachable").build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("api", "Yahoo Finance API is unreachable")
                .build();
        }
    }
}
```

This will add a `yahooFinance` component to the health response:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "mongo": { "status": "UP" },
    "yahooFinance": { "status": "UP", "details": { "api": "Yahoo Finance API is reachable" } }
  }
}
```

---

## Troubleshooting

### Health endpoint returns 404

**Problem:** `/actuator/health` returns 404 Not Found

**Solutions:**
1. Verify actuator dependency in pom.xml (already present)
2. Check `management.endpoints.web.exposure.include=health,info` in application.properties
3. Restart the application

### Health shows status "UNKNOWN"

**Problem:** Health component shows `"status": "UNKNOWN"`

**Solution:** Check if the database is running:
```bash
docker-compose ps
docker-compose logs postgres
docker-compose logs mongodb
```

### Prod mode shows full health details

**Problem:** Unauthenticated requests in prod show full health details

**Solution:** Verify `management.endpoint.health.show-details=when-authorized` in application-prod.properties

### Actuator endpoints blocked by security

**Problem:** `/actuator/health` returns 401 Unauthorized in prod

**Solution:** Verify SecurityConfig.java permits actuator endpoints:
```java
.requestMatchers("/actuator/health", "/actuator/info").permitAll()
```

---

## Summary

✅ **Actuator dependency** - Already in pom.xml
✅ **Minimal safe endpoints** - Only `/actuator/health` and `/actuator/info` exposed
✅ **DB health checks** - PostgreSQL and MongoDB auto-configured
✅ **Dev mode** - Full health details without authentication
✅ **Prod mode** - Minimal health details publicly, full details when authenticated
✅ **Security** - Both dev and prod configs permit actuator endpoints appropriately
✅ **Docker/Kubernetes ready** - Health probes enabled in prod

**Your backend is now production-ready with proper health monitoring!** 🎉
