# 🚀 Quick Deployment Guide

## Fastest Option: Railway (Recommended) ⭐

### Step 1: Sign Up
1. Go to https://railway.app
2. Sign up with GitHub

### Step 2: Deploy
1. Click "New Project"
2. Select "Deploy from GitHub repo"
3. Choose your `payment-service` repository
4. Railway auto-detects Spring Boot

### Step 3: Add Database
1. In your project, click "New"
2. Select "Database" → "Add PostgreSQL"
3. Railway creates database automatically

### Step 4: Set Environment Variables
In Railway → Your Service → Variables, add:

```bash
# Required
PLAID_CLIENT_ID=your_plaid_client_id
PLAID_SECRET=your_plaid_secret
ENCRYPTION_PASSWORD=your_strong_password_here

# Auth0
AUTH0_DOMAIN=alphabytes.us.auth0.com
AUTH0_CLIENT_ID=gbg28ZjV0MZbXAgvPgfjP1AUxOE3HfxF
AUTH0_REDIRECT_URI=https://budget-guard-app.vercel.app/app/budget/dashboard
AUTH0_AUDIENCE=https://alphabytes.us.auth0.com/api/v2/

# CORS
ALLOWED_ORIGINS=https://budget-guard-app.vercel.app

# Optional
LOG_LEVEL=INFO
SPRING_PROFILES_ACTIVE=cloud
```

**Note:** Railway automatically provides `DATABASE_URL`, `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` - you don't need to set these manually!

### Step 5: Deploy
Railway automatically:
- Builds your app
- Deploys it
- Provides a public URL (e.g., `your-app.railway.app`)

### Step 6: Update Frontend
In Vercel, update environment variable:
- `VITE_API_BASE_URL` = `https://your-app.railway.app`

---

## Alternative: Render (Free Tier)

### Step 1: Sign Up
1. Go to https://render.com
2. Sign up with GitHub

### Step 2: Create Database
1. Dashboard → "New" → "PostgreSQL"
2. Choose free tier
3. Note the connection string

### Step 3: Create Web Service
1. Dashboard → "New" → "Web Service"
2. Connect GitHub repo → Select `payment-service`
3. Settings:
   - **Environment:** Java
   - **Build Command:** `./gradlew build -x test`
   - **Start Command:** `java -jar build/libs/payment-service-0.0.1-SNAPSHOT.jar`

### Step 4: Set Environment Variables
Same as Railway (see above)

### Step 5: Deploy
Render builds and deploys automatically. You'll get a URL like:
`your-app.onrender.com`

**Note:** Free tier services spin down after 15 min of inactivity. First request may be slow.

---

## 🧪 Test Your Deployment

```bash
# Health check
curl https://your-backend-url/api/config

# Should return JSON with Auth0, API, and UI config
```

---

## ✅ Post-Deployment Checklist

- [ ] Backend URL is accessible
- [ ] `/api/config` endpoint returns JSON
- [ ] Frontend `VITE_API_BASE_URL` updated
- [ ] Auth0 redirect URI updated to production URL
- [ ] CORS allows your frontend domain
- [ ] Database migrations ran (Liquibase auto-runs on startup)

---

## 🆘 Common Issues

**Issue:** Application won't start
- Check Railway/Render logs
- Verify all required environment variables are set
- Check database connection

**Issue:** Database connection fails
- Verify `DATABASE_URL` or individual DB variables
- Check database is accessible
- Ensure credentials are correct

**Issue:** CORS errors
- Verify `ALLOWED_ORIGINS` includes your frontend URL
- Check frontend is using correct backend URL

---

**That's it!** Your backend should be live in ~5 minutes with Railway! 🎉

