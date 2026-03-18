# MarketLens - Quick Deployment Guide

## Frontend Deployment to Vercel (1 Command!)

### Prerequisites
You need a Vercel account (free). Sign up at https://vercel.com

### Deploy in 30 Seconds

```bash
# 1. Install Vercel CLI (one-time)
npm i -g vercel

# 2. Navigate to frontend directory
cd /app/frontend

# 3. Deploy!
vercel --prod

# Follow the prompts:
# - "Set up and deploy?" → Yes
# - "Which scope?" → Select your account
# - "Link to existing project?" → No
# - "Project name?" → marketlens (or your choice)
# - "Directory?" → ./ (press Enter)
# - Build settings detected? → Yes

# That's it! You'll get a URL like: https://marketlens.vercel.app
```

### Important: Update Backend URL

After deployment, your frontend will still point to the development backend.

**Option A: Use Current Backend (Temporary)**
The app will work with the current backend at:
`https://analyst-hub-22.preview.emergentagent.com`

**Option B: Deploy Backend (Recommended for Production)**

See BACKEND_DEPLOYMENT.md for instructions.

---

## Backend Deployment (Optional)

The backend is already running locally. For production:

### Option 1: Railway (Free Tier)
```bash
# Install Railway CLI
npm i -g @railway/cli

# Login
railway login

# Deploy
cd /app/backend-java
railway up

# Set environment variables in Railway dashboard:
# - SPRING_DATASOURCE_URL=<railway-postgres-url>
# - SPRING_DATA_MONGODB_URI=<mongodb-atlas-url>
```

### Option 2: Render (Free Tier)
1. Push code to GitHub
2. Go to https://render.com
3. Create new "Web Service"
4. Connect your repo
5. Set build command: `mvn clean package -DskipTests`
6. Set start command: `java -jar target/marketlens-backend-1.0.0.jar`
7. Add environment variables

---

## Quick Deploy Summary

**Fastest Path:**
```bash
cd /app/frontend
vercel --prod
```

**Production Ready:**
1. Deploy backend to Railway/Render
2. Get backend URL
3. Update frontend env: `REACT_APP_BACKEND_URL=<backend-url>`
4. Deploy frontend to Vercel

---

## Already Built

Your app is already production-built at `/app/frontend/build/`

You can also drag-and-drop deploy:
1. Go to https://app.netlify.com/drop
2. Drag the `/app/frontend/build` folder
3. Done! Instant deployment

---

## Cost

Both Vercel and Netlify offer generous free tiers:
- ✅ Unlimited deployments
- ✅ 100GB bandwidth/month
- ✅ Automatic HTTPS
- ✅ Global CDN
- ✅ Custom domains

**Total Cost: $0** 🎉
