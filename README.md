# KEYSTONE — Field Service Management Platform

Meridian Facilities Management field service platform built with **Spring Boot 3**, **React + TypeScript**, and **PostgreSQL**.

## Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 21, Spring Boot 3.3, Spring Security, JWT, JPA |
| Database | PostgreSQL 16 with Flyway migrations |
| Frontend | React 18, TypeScript, Vite |
| API Docs | springdoc-openapi (Swagger UI) |
| Build | Maven (backend), npm (frontend) |
| Local Dev | Docker Compose (frontend + backend + PostgreSQL) |
| Production | Render (Backend Docker, Frontend Static Site, Render PostgreSQL) |

## Architecture

```
keystone/
├── backend/              Spring Boot REST API (layered)
├── frontend/             React SPA (role-based views)
├── docker-compose.yml    Local development only
├── render.yaml           Render production blueprint
└── README.md
```

Requests flow: **React SPA → REST Controllers → Services → Repositories → PostgreSQL**

---

## Quick Start — Docker (Recommended for Local Dev)

**Prerequisites:** Docker Desktop

```bash
docker compose up --build
```

This starts:

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 (user/pass/db: `keystone`) |

PostgreSQL data persists in the named Docker volume `keystone_pg_data`.

---

## Manual Local Setup (without Docker)

### Prerequisites

- Java 21, Maven 3.9+, Node.js 18+, PostgreSQL 16

### Database

```sql
CREATE DATABASE keystone;
CREATE USER keystone WITH PASSWORD 'keystone';
GRANT ALL PRIVILEGES ON DATABASE keystone TO keystone;
```

Or set credentials via environment variables (see below).

### Backend (port 8080)

```bash
cd backend
mvn spring-boot:run
```

### Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

---

## Environment Variables

All production and Docker settings are configured via environment variables:

| Variable | Description | Default (local) |
|----------|-------------|-----------------|
| `SPRING_DATASOURCE_URL` | JDBC or `postgresql://` URL | `jdbc:postgresql://localhost:5432/keystone` |
| `SPRING_DATASOURCE_USERNAME` | Database user | `keystone` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `keystone` |
| `DATABASE_URL` | Render/Heroku-style URL (auto-converted to JDBC) | — |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | Dev default |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `86400000` |
| `KEYSTONE_CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:5173` |
| `PORT` | HTTP port (set by Render) | `8080` |
| `VITE_API_URL` | Frontend API base URL (build-time, Render Static Site) | Empty (uses proxy/nginx) |

---

## Render Deployment

**Do not use `docker-compose.yml` in production.** Use Render services:

| Component | Render Type |
|-----------|-------------|
| Backend | **Docker Web Service** (`backend/Dockerfile`) |
| Frontend | **Static Site** (no frontend Dockerfile required) |
| Database | **Render PostgreSQL** |

### Steps

1. Push the repo to GitHub.
2. Create a **Render PostgreSQL** database named `keystone-db`.
3. Create a **Docker Web Service** for the backend:
   - Dockerfile path: `backend/Dockerfile`
   - Docker context: `backend`
   - Link the PostgreSQL database
   - Set environment variables (or use `render.yaml` blueprint):
     - `SPRING_DATASOURCE_URL` — from database connection string (auto-converted)
     - `SPRING_DATASOURCE_USERNAME` — from database
     - `SPRING_DATASOURCE_PASSWORD` — from database
     - `JWT_SECRET` — generate a secure random value
     - `KEYSTONE_CORS_ALLOWED_ORIGINS` — your frontend URL, e.g. `https://keystone-frontend.onrender.com`
4. Create a **Static Site** for the frontend:
   - Build command: `cd frontend && npm ci && npm run build`
   - Publish directory: `frontend/dist`
   - Set `VITE_API_URL` to your backend URL, e.g. `https://keystone-backend.onrender.com`

Alternatively, deploy everything with the included **`render.yaml`** blueprint.

---

## Seed Users

All seed users share the password: **`password123`**

| Role | Email |
|------|-------|
| Manager | manager@meridian.com |
| Dispatcher | dispatcher@meridian.com |
| Technician | tech1@meridian.com |
| Customer | customer@acme.com |

---

## MySQL → PostgreSQL Migration Summary

Files modified for the database migration:

| File | Change |
|------|--------|
| `backend/pom.xml` | `postgresql` driver; removed `mysql-connector-j` and `flyway-mysql` |
| `backend/src/main/resources/application.properties` | PostgreSQL JDBC URL, dialect, env-var placeholders |
| `backend/src/main/resources/db/migration/V1__initial_schema.sql` | `BIGSERIAL`, removed MySQL `AUTO_INCREMENT` / `ON UPDATE` |
| `backend/src/main/resources/db/migration/V2__seed_data.sql` | PostgreSQL intervals, sequence resets |
| `backend/src/main/java/com/keystone/config/PostgresEnvironmentPostProcessor.java` | Converts `postgresql://` URLs for Render |
| `README.md` | Updated documentation |

No business logic, API, auth, or UI code was changed.

---

## Testing

```bash
cd backend
mvn test
```

---

**Project KEYSTONE** · Zidio Development · Java Full-Stack Engineering
