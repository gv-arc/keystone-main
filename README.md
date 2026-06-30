# KEYSTONE — Field Service Management Platform

KEYSTONE is the system Meridian Facilities Management uses to run its maintenance operation end to end — from a customer reporting a problem, through dispatch and field work, to a closed and accounted-for job. It replaces spreadsheets, phone calls, and messaging apps with one platform covering four roles: **dispatcher**, **technician**, **manager/admin**, and **customer**.

Built as a Java Full-Stack Engineering project for Zidio Development.

## Live Deployment

| Component | URL |
|---|---|
| Frontend (React SPA) | https://keystone-ritik-singh.onrender.com |
| Backend API | https://keystone-backend-752r.onrender.com |
| Swagger / OpenAPI UI | https://keystone-backend-752r.onrender.com/swagger-ui.html |
| Database | Render PostgreSQL (managed) |

> **Cold start note:** services are on Render's free tier and spin down when idle. Before using the app, open the Backend API and Frontend links above first and wait ~3 min for them to wake up, then use the Frontend as the full-stack app.

### Seed logins

All seed users share the password `password123`.

| Role | Email |
|---|---|
| Manager | manager@meridian.com |
| Dispatcher | dispatcher@meridian.com |
| Technician | tech1@meridian.com |
| Technician | tech2@meridian.com |
| Customer (Acme Corp) | customer@acme.com |
| Customer (Global Retail) | customer@globalretail.com |

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3.5, Spring Security, JWT (jjwt), Spring Data JPA, Bean Validation |
| Database | PostgreSQL with Flyway migrations |
| Frontend | React 18, TypeScript, Vite, React Router |
| API Docs | springdoc-openapi (Swagger UI) |
| Build | Maven (backend), npm (frontend) |
| Local Dev | Docker Compose (frontend + backend + PostgreSQL) — see `docker-compose.yml` |
| Production | Render (Backend Docker Web Service, Frontend Static Site, Render PostgreSQL) |

## Architecture

```
keystone/
├── backend/              Spring Boot REST API (layered)
├── frontend/             React SPA (role-based views)
├── docker-compose.yml    Local development only
└── README.md
```

Requests flow: **React SPA → REST Controllers → Services → Repositories → PostgreSQL**

- **Controllers** — HTTP only: input validation, auth, DTO mapping, delegate to services.
- **Services** — business rules, the work-order state machine (`WorkOrderStateMachine`), SLA logic; transactional boundaries.
- **Repositories** — Spring Data JPA, queries scoped so customers only read their own data.
- **Persistence** — PostgreSQL, schema managed entirely by Flyway migrations.
- **Cross-cutting** — stateless JWT auth (`JwtAuthenticationFilter`, `SecurityConfig`), no server-side session.

### Domain model

`Customer` → has many `Site`s → raise `WorkOrder`s, assigned to a `User` (technician), each accumulating:
- `WorkOrderStatusHistory` — append-only audit trail of every status change
- `PartUsage` — parts consumed, decrementing `Part` stock transactionally
- `TimeLog` — labour minutes logged against the job

### Work-order lifecycle

```
NEW → ASSIGNED → IN_PROGRESS → COMPLETED → CLOSED
                     │↕ ON_HOLD
NEW/ASSIGNED ───────────────────────→ CANCELLED
```

Transitions are enforced in the service layer, not just the UI — illegal jumps return `409`, and every transition writes a status-history row.

### User roles & access

| Role | Can do |
|---|---|
| Dispatcher | Create customers, sites, and work orders; assign to technicians; view all jobs and the board. |
| Technician | View only their assigned jobs; start/hold/resume/complete; log parts and time. |
| Manager / Admin | Everything a dispatcher can do, plus close jobs, manage users and parts, and view all reports. |
| Customer | Raise requests for their own sites; view status/history of their own work orders only. |

Authorisation is enforced server-side on every request (Spring Security method-level checks) — hiding a control in the UI is never the security boundary. A customer cannot fetch another customer's data by changing an ID, and a technician cannot act on jobs not assigned to them, even via direct API calls.

---

## Features

| # | Feature | Covers |
|---|---|---|
| F1 | Authentication & roles | JWT login, 4 roles, BCrypt passwords, server-enforced permissions |
| F2 | Customers & sites | CRUD, searchable/paginated lists, customer data isolation |
| F3 | Work-order management | Create/edit with validation, unique human-readable codes, immutable once closed |
| F4 | Dispatch & assignment | Assign/reassign, Kanban board by status |
| F5 | Technician field view | Assigned jobs only, start/hold/complete, mobile-responsive |
| F6 | Parts & time logging | Transactional stock decrement, time entries, rollup totals |
| F7 | SLA tracking & notifications | Priority-based due dates, scheduled breach checks, manager alerts |
| F8 | Dashboard & reporting | Status counts, overdue work, SLA compliance, technician/site breakdown |
| F9 | Customer portal | Self-service request raising and status tracking, scoped to own data |

---

## API Overview

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/auth/login` | POST | Authenticate; returns a JWT |
| `/api/customers` | GET / POST | List or create customers |
| `/api/customers/{id}/sites` | GET / POST | Sites for a customer |
| `/api/work-orders` | GET / POST | List (role-scoped, paged, filterable) or create |
| `/api/work-orders/{id}` | GET | Fetch one work order with its status history |
| `/api/work-orders/{id}/assign` | POST | Assign to a technician |
| `/api/work-orders/{id}/status` | POST | Transition status (`409` if illegal) |
| `/api/work-orders/{id}/parts` | POST | Log parts used (transactional stock decrement) |
| `/api/work-orders/{id}/time` | POST | Log time against the job |
| `/api/customer-requests` | POST | Public-facing request intake (customer portal) |
| `/api/reports/summary` | GET | Dashboard metrics |

Full, accurate, browsable reference: see Swagger UI link above. Status codes follow convention — `200`/`201` success, `400` validation, `401`/`403` auth, `404` missing, `409` illegal transition — and errors return a consistent structured JSON body, never a stack trace.

---

## Quick Start — Docker (Recommended for Local Dev)

**Prerequisites:** Docker Desktop

```bash
docker compose up --build
```

This starts:

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 (user/pass/db: `keystone`) |

PostgreSQL data persists in the named Docker volume `keystone_pg_data`. Flyway runs migrations and seed data automatically on backend startup.

---

## Manual Local Setup (without Docker)

### Prerequisites

- Java 21, Maven 3.9+, Node.js 20+, PostgreSQL 14+

### Database

```sql
CREATE DATABASE keystone;
CREATE USER keystone WITH PASSWORD 'keystone';
GRANT ALL PRIVILEGES ON DATABASE keystone TO keystone;
```

Or point at any PostgreSQL instance via the environment variables below.

### Backend (port 8080)

```bash
cd backend
mvn spring-boot:run
```

Flyway runs `V1__initial_schema.sql` and `V2__seed_data.sql` automatically on startup.

### Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

In local dev, Vite's built-in proxy (`vite.config.ts`) forwards `/api/*` to `http://localhost:8080`, so `VITE_API_URL` is not required locally.

---

## Environment Variables

| Variable | Description | Default (local) |
|---|---|---|
| `SPRING_DATASOURCE_URL` | **JDBC** URL (`jdbc:postgresql://host:port/db`) | `jdbc:postgresql://localhost:5432/keystone` |
| `SPRING_DATASOURCE_USERNAME` | Database user | `keystone` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `keystone` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | Dev default in `application.properties` — override in production |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `86400000` (24h) |
| `KEYSTONE_CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins, **no trailing slashes** | `http://localhost:5173,http://localhost:3000` |
| `PORT` | HTTP port (Render sets this automatically) | `8080` |
| `VITE_API_URL` | Frontend build-time API base URL, **no trailing slash** | empty (Vite proxy used instead) |


---

## Submission

| Item | Status |
|---|---|
| Git repository (backend + frontend) | ✅ |
| Live URLs — API, frontend, Swagger | ✅ See *Live Deployment* above |
| Seed logins for all four roles | ✅ See *Seed logins* above |
| README — overview, stack, setup, env vars, migrations, architecture | ✅ This document |
| Demo video (3–5 min, unlisted link) | ⬜ https://drive.google.com/file/d/1rri86TOlMgHsjsFtZ01XAM0L0wN_jVGE/view?usp=drivesdk |

---

**Project KEYSTONE** · Zidio Development · Java Full-Stack Engineering
