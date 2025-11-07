# Backend Deployment Guide - Payment Service

This guide covers deploying your Spring Boot backend to various cloud platforms.

---

## 🚀 Recommended Platforms

### Option 1: Railway (Easiest - Recommended) ⭐
- **Free tier available**
- **Automatic HTTPS**
- **PostgreSQL included**
- **Easy environment variable management**
- **GitHub integration**

### Option 2: Render
- **Free tier available**
- **PostgreSQL included**
- **Automatic deployments**
- **Good for Spring Boot**

### Option 3: Heroku
- **Paid (no free tier)**
- **Very reliable**
- **Easy setup**
- **PostgreSQL addon**

### Option 4: AWS/GCP/Azure
- **More complex setup**
- **More control**
- **Enterprise-grade**

---

## 📋 Pre-Deployment Checklist

Before deploying, ensure you have:

- [ ] PostgreSQL database (or use platform's managed database)
- [ ] All environment variables ready
- [ ] GitHub repository connected
- [ ] Domain name (optional, for custom domain)

---

## 🚂 Option 1: Deploy to Railway (Recommended)

### Step 1: Create Railway Account
1. Go to https://railway.app
2. Sign up with GitHub
3. Create a new project

### Step 2: Add PostgreSQL Database
1. In Railway dashboard, click "New"
2. Select "Database" → "Add PostgreSQL"
3. Railway will create a database and provide connection string

### Step 3: Deploy Your Backend
1. In Railway dashboard, click "New"
2. Select "GitHub Repo"
3. Choose your `payment-service` repository
4. Railway will auto-detect it's a Spring Boot app

### Step 4: Configure Environment Variables

In Railway dashboard → Your Service → Variables, add:

**Required:**
```
PLAID_CLIENT_ID=your_plaid_client_id
PLAID_SECRET=your_plaid_secret
ENCRYPTION_PASSWORD=your_strong_encryption_password
POSTGRES_DB=railway
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<from_railway_database>
POSTGRES_HOST=<from_railway_database>
POSTGRES_PORT=5432
```

**Database Connection:**
```
DATABASE_URL=<from_railway_database> (Railway provides this automatically)
```

**Optional:**
```
ALLOWED_ORIGINS=https://budget-guard-app.vercel.app,https://your-domain.com
AUTH0_DOMAIN=alphabytes.us.auth0.com
AUTH0_CLIENT_ID=gbg28ZjV0MZbXAgvPgfjP1AUxOE3HfxF
AUTH0_REDIRECT_URI=https://budget-guard-app.vercel.app/app/budget/dashboard
LOG_LEVEL=INFO
```

### Step 5: Update Database URL

Railway provides `DATABASE_URL` in format: `postgresql://user:pass@host:port/dbname`

You may need to parse it or use Railway's provided variables:
- `PGHOST`
- `PGPORT`
- `PGUSER`
- `PGPASSWORD`
- `PGDATABASE`

### Step 6: Configure Build Settings

Railway should auto-detect, but verify:
- **Build Command:** `./gradlew build`
- **Start Command:** `java -jar build/libs/payment-service-0.0.1-SNAPSHOT.jar`
- **Root Directory:** `./` (root of repo)

### Step 7: Deploy

Railway will automatically:
1. Build your app
2. Deploy it
3. Provide a public URL (e.g., `your-app.railway.app`)

### Step 8: Update Frontend

Update your Vercel environment variable:
- `VITE_API_BASE_URL` = `https://your-app.railway.app`

---

## 🎨 Option 2: Deploy to Render

### Step 1: Create Render Account
1. Go to https://render.com
2. Sign up with GitHub

### Step 2: Create PostgreSQL Database
1. Dashboard → "New" → "PostgreSQL"
2. Choose free tier
3. Note the connection details

### Step 3: Create Web Service
1. Dashboard → "New" → "Web Service"
2. Connect your GitHub repository
3. Select `payment-service` repository

### Step 4: Configure Service

**Build Settings:**
- **Environment:** Java
- **Build Command:** `./gradlew build`
- **Start Command:** `java -jar build/libs/payment-service-0.0.1-SNAPSHOT.jar`

**Environment Variables:**
Add all the same variables as Railway (see above)

### Step 5: Deploy

Render will build and deploy automatically. You'll get a URL like:
`your-app.onrender.com`

**Note:** Free tier services spin down after inactivity (15 min). First request may be slow.

---

## 🐳 Option 3: Deploy with Docker (Any Platform)

### Step 1: Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 2: Create .dockerignore

```
.gradle
build
.idea
*.iml
.git
.gitignore
```

### Step 3: Deploy to Any Platform

- **Railway:** Supports Docker
- **Render:** Supports Docker
- **AWS ECS/Fargate:** Use Docker
- **Google Cloud Run:** Use Docker
- **Azure Container Instances:** Use Docker

---

## ☁️ Option 4: AWS Deployment

### Option 4A: AWS Elastic Beanstalk (Easiest)

1. **Install EB CLI:**
   ```bash
   pip install awsebcli
   ```

2. **Initialize:**
   ```bash
   cd payment-service
   eb init
   ```

3. **Create Environment:**
   ```bash
   eb create production-env
   ```

4. **Set Environment Variables:**
   ```bash
   eb setenv PLAID_CLIENT_ID=xxx PLAID_SECRET=xxx ...
   ```

5. **Deploy:**
   ```bash
   eb deploy
   ```

### Option 4B: AWS ECS/Fargate

1. Build Docker image
2. Push to ECR (Elastic Container Registry)
3. Create ECS task definition
4. Deploy to Fargate
5. Configure ALB (Application Load Balancer)

---

## 🔧 Required Environment Variables

### Critical (Must Set):
```bash
# Plaid
PLAID_CLIENT_ID=your_client_id
PLAID_SECRET=your_secret
PLAID_ENV=sandbox  # or 'production' for live

# Encryption
ENCRYPTION_PASSWORD=your_strong_password_here

# Database
POSTGRES_DB=your_db_name
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_db_password
POSTGRES_HOST=your_db_host
POSTGRES_PORT=5432
# OR use DATABASE_URL format: postgresql://user:pass@host:port/dbname

# Auth0
AUTH0_DOMAIN=alphabytes.us.auth0.com
AUTH0_CLIENT_ID=gbg28ZjV0MZbXAgvPgfjP1AUxOE3HfxF
AUTH0_REDIRECT_URI=https://your-frontend-url/app/budget/dashboard
AUTH0_AUDIENCE=https://alphabytes.us.auth0.com/api/v2/
```

### Optional:
```bash
# CORS
ALLOWED_ORIGINS=https://budget-guard-app.vercel.app,https://your-domain.com

# API URLs
API_BASE_URL=https://your-backend-url
PLAID_API_URL=https://your-backend-url

# Logging
LOG_LEVEL=INFO
SPRING_PROFILES_ACTIVE=prod
```

---

## 📝 Database Migration

### Option 1: Automatic (Liquibase)
Liquibase will run automatically on startup if:
- Database is accessible
- `spring.liquibase.enabled=true` (default)

### Option 2: Manual
If you need to run migrations manually:
```bash
./gradlew liquibaseUpdate
```

---

## 🔒 Security Checklist for Production

- [ ] All secrets in environment variables (not in code)
- [ ] `ENCRYPTION_PASSWORD` set with strong password
- [ ] Database credentials secure
- [ ] CORS configured for production frontend only
- [ ] HTTPS enabled (most platforms do this automatically)
- [ ] `SPRING_PROFILES_ACTIVE=prod` set
- [ ] `LOG_LEVEL=INFO` (not DEBUG)
- [ ] Auth0 redirect URI updated to production URL
- [ ] Plaid environment set to `production` (when ready)

---

## 🧪 Post-Deployment Testing

1. **Health Check:**
   ```bash
   curl https://your-backend-url/api/config
   ```

2. **CORS Test:**
   ```bash
   curl -H "Origin: https://budget-guard-app.vercel.app" \
        -H "Access-Control-Request-Method: POST" \
        -X OPTIONS https://your-backend-url/api/user/transactions
   ```

3. **Security Headers:**
   ```bash
   curl -I https://your-backend-url/api/config
   # Should see X-Frame-Options, X-Content-Type-Options, etc.
   ```

4. **JWT Validation:**
   - Try with invalid token - should get 401
   - Try with valid Auth0 token - should work

---

## 🔄 Continuous Deployment

Most platforms support automatic deployments:

1. **Railway:** Auto-deploys on push to main branch
2. **Render:** Auto-deploys on push to main branch
3. **Heroku:** Auto-deploys if GitHub connected
4. **AWS:** Use CodePipeline or GitHub Actions

---

## 📊 Monitoring & Logs

### Railway:
- View logs in dashboard
- Metrics available
- Alerts configurable

### Render:
- View logs in dashboard
- Metrics available
- Email alerts

### AWS:
- CloudWatch for logs
- CloudWatch Metrics
- Set up alarms

---

## 🆘 Troubleshooting

### Issue: Application won't start
- Check logs for errors
- Verify all environment variables are set
- Check database connectivity
- Verify port configuration (most platforms set PORT automatically)

### Issue: Database connection fails
- Verify DATABASE_URL or individual DB variables
- Check database is accessible from your app
- Verify credentials are correct

### Issue: CORS errors
- Verify `ALLOWED_ORIGINS` includes your frontend URL
- Check CORS configuration in SecurityConfig
- Ensure frontend is using correct backend URL

### Issue: JWT validation fails
- Verify Auth0 domain is correct
- Check Auth0 JWKS endpoint is accessible
- Verify token format is correct

---

## 🎯 Quick Start: Railway (5 minutes)

1. Go to https://railway.app
2. Sign up with GitHub
3. New Project → Deploy from GitHub repo
4. Select `payment-service` repository
5. Add PostgreSQL database
6. Set environment variables
7. Deploy!

Railway will:
- Auto-detect Spring Boot
- Build your app
- Deploy it
- Give you a public URL

---

## 📚 Additional Resources

- [Railway Docs](https://docs.railway.app)
- [Render Docs](https://render.com/docs)
- [Spring Boot Deployment](https://spring.io/guides/gs/spring-boot-for-azure/)
- [Docker Spring Boot](https://spring.io/guides/gs/spring-boot-docker/)

---

**Recommended:** Start with **Railway** - it's the easiest and has a free tier perfect for getting started!

