# MarketLens Deployment Guide

## 🚀 Quick Deploy

MarketLens frontend is ready to deploy to Vercel or Netlify with zero configuration!

## Option 1: Deploy to Vercel (Recommended)

### One-Click Deploy
[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone)

### Manual Deploy via CLI

```bash
# Install Vercel CLI
npm i -g vercel

# Navigate to frontend directory
cd /app/frontend

# Login to Vercel
vercel login

# Deploy to production
vercel --prod
```

### Deploy via Vercel Dashboard

1. Go to [vercel.com](https://vercel.com)
2. Click "Add New Project"
3. Import your Git repository
4. Vercel will auto-detect React app
5. Click "Deploy"

**Build Settings** (Auto-detected):
- Framework Preset: `Create React App`
- Build Command: `yarn build`
- Output Directory: `build`
- Install Command: `yarn install`

**No environment variables needed** - App works with mock data!

---

## Option 2: Deploy to Netlify

### One-Click Deploy
[![Deploy to Netlify](https://www.netlify.com/img/deploy/button.svg)](https://app.netlify.com/start)

### Manual Deploy via CLI

```bash
# Install Netlify CLI
npm install -g netlify-cli

# Navigate to frontend directory
cd /app/frontend

# Build the project
yarn build

# Login to Netlify
netlify login

# Deploy to production
netlify deploy --prod --dir=build
```

### Deploy via Drag & Drop

1. Build the project locally:
   ```bash
   cd /app/frontend
   yarn build
   ```

2. Go to [app.netlify.com/drop](https://app.netlify.com/drop)

3. Drag and drop the `build` folder

4. Your site is live!

### Deploy via Netlify Dashboard

1. Go to [app.netlify.com](https://app.netlify.com)
2. Click "Add new site" → "Import an existing project"
3. Connect to your Git provider
4. Select your repository
5. Configure build settings:
   - Build command: `yarn build`
   - Publish directory: `build`
6. Click "Deploy site"

**No environment variables needed** - App works with mock data!

---

## 🔧 Build Configuration

Both platforms auto-detect the configuration, but here's what's used:

### Package.json Scripts
```json
{
  "scripts": {
    "start": "craco start",
    "build": "craco build",
    "test": "craco test"
  }
}
```

### Vercel Configuration (`vercel.json`)
Already included in the project. Handles:
- SPA routing (all routes → index.html)
- Static asset caching
- Optimized builds

### Netlify Configuration (`netlify.toml`)
Already included in the project. Handles:
- SPA routing redirects
- Build settings

---

## 🌐 Custom Domain Setup

### Vercel
1. Go to your project settings
2. Click "Domains"
3. Add your custom domain
4. Update DNS records as instructed

### Netlify
1. Go to "Domain settings"
2. Click "Add custom domain"
3. Follow DNS configuration steps

---

## 🔍 Post-Deployment Checklist

After deployment, verify:

- [ ] Landing page loads correctly
- [ ] "Try Demo (AAPL)" button works
- [ ] Preset ticker buttons (MSFT, TSLA, SPY, NVDA) work
- [ ] Dashboard displays charts correctly
- [ ] All 4 KPI cards show metrics
- [ ] Price chart renders
- [ ] Returns chart renders
- [ ] Volatility chart renders
- [ ] RSI chart renders with thresholds
- [ ] Methodology section is visible
- [ ] Download JSON button works
- [ ] Case Study page loads
- [ ] Navigation works between pages
- [ ] Mobile responsive design works
- [ ] No console errors

---

## 🐛 Troubleshooting

### Build Fails

**Issue**: `Module not found` errors

**Solution**:
```bash
cd /app/frontend
rm -rf node_modules yarn.lock
yarn install
yarn build
```

**Issue**: Out of memory during build

**Solution**: Increase Node memory
```bash
NODE_OPTIONS=--max-old-space-size=4096 yarn build
```

### Routing Issues (404 on refresh)

**Vercel**: Should work automatically with `vercel.json`

**Netlify**: Should work automatically with `netlify.toml`

If not working, ensure redirect rules are set:
- Vercel: Check `vercel.json` is in root
- Netlify: Check `netlify.toml` is in root

### Charts Not Rendering

**Issue**: Recharts not displaying

**Solution**: Clear browser cache or check console for errors

---

## 📊 Performance Optimization

Both platforms provide automatic optimizations:

- ✅ CDN distribution
- ✅ Automatic HTTPS
- ✅ Gzip compression
- ✅ Image optimization
- ✅ Asset caching
- ✅ Edge network delivery

### Additional Optimizations

1. **Enable Analytics** (Vercel/Netlify)
2. **Set up monitoring** (uptime, performance)
3. **Configure custom headers** (security, caching)

---

## 🔐 Security Headers (Optional)

Add to Vercel (`vercel.json`):
```json
{
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        {
          "key": "X-Content-Type-Options",
          "value": "nosniff"
        },
        {
          "key": "X-Frame-Options",
          "value": "DENY"
        },
        {
          "key": "X-XSS-Protection",
          "value": "1; mode=block"
        }
      ]
    }
  ]
}
```

Add to Netlify (`netlify.toml`):
```toml
[[headers]]
  for = "/*"
  [headers.values]
    X-Frame-Options = "DENY"
    X-Content-Type-Options = "nosniff"
    X-XSS-Protection = "1; mode=block"
```

---

## 💰 Cost

Both platforms offer generous free tiers:

### Vercel Free Tier
- ✅ Unlimited deployments
- ✅ 100GB bandwidth/month
- ✅ Automatic HTTPS
- ✅ Custom domains

### Netlify Free Tier
- ✅ Unlimited personal projects
- ✅ 100GB bandwidth/month
- ✅ Automatic HTTPS
- ✅ Custom domains

---

## 🔄 Continuous Deployment

Both platforms support automatic deployments:

1. **Connect Git repository** (GitHub, GitLab, Bitbucket)
2. **Push to main branch**
3. **Automatic build & deploy**

### Branch Previews
- Every PR gets a preview URL
- Test changes before merging
- Share with stakeholders

---

## 📱 Mobile Testing

After deployment, test on:
- iPhone (Safari)
- Android (Chrome)
- iPad (Safari)
- Various screen sizes

Use browser dev tools for responsive testing.

---

## 🎯 Next Steps After Deployment

1. **Share the link** with stakeholders
2. **Test all features** thoroughly
3. **Monitor performance** via platform analytics
4. **Set up custom domain** (optional)
5. **Enable analytics** (Google Analytics, etc.)
6. **Add to portfolio/resume**

---

## 🆘 Support

If you encounter issues:

1. Check platform status pages:
   - [Vercel Status](https://www.vercel-status.com/)
   - [Netlify Status](https://www.netlifystatus.com/)

2. Review build logs in dashboard

3. Check console for JavaScript errors

4. Verify all files are committed to Git

---

## 🎉 Success!

Once deployed, your MarketLens application will be:
- ✅ Live and accessible worldwide
- ✅ Fast and reliable (CDN-powered)
- ✅ Secure (HTTPS enabled)
- ✅ Professional (custom domain ready)

**Example URLs**:
- Vercel: `https://marketlens-yourname.vercel.app`
- Netlify: `https://marketlens-yourname.netlify.app`

Share your deployed link and showcase your work! 🚀
