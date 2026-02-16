# 🚀 Deployment Guide - MarketLens Backend

Complete guide for deploying the MarketLens Spring Boot backend locally and to production (Render).

---

## Quick Start

```bash
# Local development with Docker
docker-compose up -d

# Test backend
curl http://localhost:8001/actuator/health
```

---

## Table of Contents

1. [Local Development](#local-development-docker-compose)
2. [Docker Build & Run](#docker-build--run)
3. [Render Deployment](#render-deployment)
4. [Environment Variables](#environment-variables)
5. [Troubleshooting](#troubleshooting)

---

## Local Development (Docker Compose)

### Prerequisites

- Docker Desktop (includes Docker Compose)
- Git

### Start All Services

```bash
# From project root
docker-compose up -d

# View logs
docker-compose logs -f backend

# Expected: "🔐 PROD MODE: Security enabled"
```

### Verify Services

**Health Check:**
```bash
curl http://localhost:8001/actuator/health | jq

# Expected:
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "mongo": {"status": "UP"}
  }
}
```

**Test API:**
```bash
# Note: Requires JWT in prod mode
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT" \
  -d '{"ticker":"AAPL","period":"1Y"}'
```

### Stop Services

```bash
# Stop (keep data)
docker-compose down

# Stop and delete data
docker-compose down -v
```

### Rebuild After Code Changes

```bash
docker-compose build backend
docker-compose up -d --force-recreate backend
```

---

## Docker Build & Run

### Build Image

```bash
cd backend-java
docker build -t marketlens-backend:latest .
```

### Run Standalone

```bash
docker run -d \
  -p 8001:8001 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD=postgres \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/marketlens \
  marketlens-backend:latest
```

---

## Render Deployment

### Quick Deploy (Blueprint Method)

1. **Push code to GitHub**
2. **Go to Render** → https://dashboard.render.com
3. **New** → **Blueprint**
4. Connect repo, select `main` branch
5. **Apply** (creates PostgreSQL + Backend)
6. **Set MongoDB URI** in dashboard (use MongoDB Atlas)
7. **Update CORS** and **JWT** settings

### Manual Deploy Steps

#### 1. Create PostgreSQL Database

Render Dashboard → **New** → **PostgreSQL**

- Name: `marketlens-postgres`
- Region: `Oregon`
- Plan: `Starter` (free)

#### 2. Create Web Service

Render Dashboard → **New** → **Web Service**

**Settings:**
- Runtime: `Docker`
- Dockerfile: `./backend-java/Dockerfile`
- Docker Context: `./backend-java`
- Region: `Oregon` (match database)
- Plan: `Starter` (free)

**Health Check:**
- Path: `/actuator/health`

#### 3. Set Environment Variables

| Variable | Value |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `PORT` | `8001` |
| `DB_HOST` | (from PostgreSQL dashboard) |
| `DB_PORT` | `5432` |
| `DB_NAME` | `marketlens` |
| `DB_USERNAME` | (from PostgreSQL dashboard) |
| `DB_PASSWORD` | (from PostgreSQL dashboard) |
| `MONGODB_URI` | `mongodb+srv://user:pass@cluster.mongodb.net/marketlens` |
| `MONGODB_DATABASE` | `marketlens` |
| `CORS_ALLOWED_ORIGINS` | `https://yourfrontend.com` |
| `JWT_ISSUER_URI` | `https://your-tenant.auth0.com/` (optional) |
| `JWT_JWK_SET_URI` | `https://your-tenant.auth0.com/.well-known/jwks.json` (optional) |

#### 4. Deploy

Click **Create Web Service** → Wait ~5-10 minutes

**Your backend:** `https://marketlens-backend.onrender.com`

---

## MongoDB Setup (MongoDB Atlas)

Render doesn't offer MongoDB. Use MongoDB Atlas (free tier):

1. **Create cluster** at https://cloud.mongodb.com
2. **Create user** with password
3. **Whitelist IPs:** `0.0.0.0/0`
4. **Get connection string:**
   ```
   mongodb+srv://username:password@cluster.mongodb.net/marketlens
   ```
5. **Add to Render:** Environment → `MONGODB_URI`

---

## Environment Variables Reference

### Required

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Profile (dev/prod) | `prod` |
| `PORT` | Server port | `8001` |
| `DB_HOST` | PostgreSQL host | `localhost` or `dpg-xxx.render.com` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `marketlens` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |
| `MONGODB_URI` | MongoDB connection | `mongodb://...` or `mongodb+srv://...` |
| `MONGODB_DATABASE` | MongoDB database | `marketlens` |
| `CORS_ALLOWED_ORIGINS` | Frontend URLs | `http://localhost:3000` |

### Optional (JWT)

| Variable | Description |
|----------|-------------|
| `JWT_ISSUER_URI` | OAuth2 issuer (e.g., Auth0) |
| `JWT_JWK_SET_URI` | JWT keys endpoint |

---

## Testing

### Health Check

```bash
# Local
curl http://localhost:8001/actuator/health

# Render
curl https://marketlens-backend.onrender.com/actuator/health
```

### API Test

```bash
curl -X POST https://marketlens-backend.onrender.com/api/analysis/run \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT" \
  -d '{"ticker":"AAPL","period":"1Y"}'
```

---

## Troubleshooting

### "Port already in use" (Local)

```bash
lsof -i :8001
kill -9 <PID>
```

### "Cannot connect to database" (Render)

- Verify `DB_HOST` uses internal connection (starts with `dpg-`)
- Check database and backend in same region
- Verify MongoDB connection string uses `mongodb+srv://`

### "CORS error" (Browser)

- Update `CORS_ALLOWED_ORIGINS` with your frontend URL
- No trailing slash: `https://app.com` not `https://app.com/`

### "401 Unauthorized" (All requests)

- Check `SPRING_PROFILES_ACTIVE=prod`
- Verify JWT configuration if using Auth0
- For testing, use `SPRING_PROFILES_ACTIVE=dev` (NOT for production)

### Free Tier Spin-Down

Render free tier spins down after 15 min inactivity:
- Cold start: ~30s
- Upgrade to Standard: $7/month (always-on)
- Or ping `/actuator/health` every 10 min

---

## Production Checklist

Before deploying to production:

- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Configure JWT authentication (Auth0/Cognito)
- [ ] Set production `CORS_ALLOWED_ORIGINS`
- [ ] Use strong `DB_PASSWORD`
- [ ] Set up MongoDB Atlas (or external MongoDB)
- [ ] Configure health checks on hosting platform
- [ ] Enable HTTPS (Render does this automatically)
- [ ] Test `/actuator/health` endpoint
- [ ] Monitor logs for errors
- [ ] Consider upgrading to Standard plan for always-on service

---

## Summary

✅ **Docker Compose** - Full stack (postgres + mongo + backend)
✅ **Multi-stage Dockerfile** - Slim image (~300MB)
✅ **Render Blueprint** - One-click deployment
✅ **Health Checks** - `/actuator/health` for monitoring
✅ **Environment-Driven** - All config via env vars
✅ **Production-Ready** - Security, logging, CORS, JWT

**Deploy now:** `docker-compose up` or push to Render! 🚀
