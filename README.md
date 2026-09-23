<div align="center">

# 🏥 Hospital Management System

### Full-Stack Hospital Management Platform

A full-stack hospital management application for managing users, doctors, patients, appointments, examinations, departments, notifications, and doctor schedules.

Built with **Spring Boot**, **Next.js**, **React**, and relational database technologies, with a focus on clean backend architecture, authentication, authorization, database migrations, testing, and production-oriented engineering practices.

<br/>

[![Build and Test](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml/badge.svg)](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-000000?style=flat&logo=next.js&logoColor=white)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat)](LICENSE)

<br/>

**Current Status: Production Hardening**

</div>

---

## 📑 Contents

* [Overview](#-overview)
* [Features](#-features)
* [Architecture](#-architecture)
* [Technology Stack](#-technology-stack)
* [Project Structure](#-project-structure)
* [Getting Started](#-getting-started)
* [Configuration](#-configuration)
* [API](#-api)
* [Authentication & Security](#-authentication--security)
* [Database](#-database)
* [Caching](#-caching)
* [Observability](#-observability)
* [Testing](#-testing)
* [Docker](#-docker)
* [CI/CD](#-cicd)
* [Deployment](#-deployment)
* [Development Notes](#-development-notes)
* [Contributing](#-contributing)
* [License](#-license)

---

# 🌟 Overview

**Hospital Management System** is a full-stack web application designed to manage common hospital workflows through a centralized platform.

The application provides different capabilities depending on the user's role, including:

* User authentication
* Patient management
* Doctor management
* Appointment scheduling
* Examination records
* Departments and polyclinics
* Doctor leave management
* Patient notifications
* Administrative management
* Dashboard and reporting interfaces

The backend is implemented with **Spring Boot** and follows a modular layered architecture. The frontend is implemented with **Next.js and React**.

The project also includes database migrations, automated tests, security controls, application metrics, request tracing, caching, Docker support, and GitHub Actions CI.

---

# ✨ Features

## 👤 User Management

* User registration
* User authentication
* Role-based access
* User information management
* Password recovery flow

Supported application roles include:

```text
ADMIN
DOCTOR
PATIENT
```

---

## 📅 Appointment Management

* Appointment creation
* Appointment listing
* Appointment updates
* Appointment cancellation
* Doctor appointment views
* Patient appointment history
* Doctor availability
* Time-slot management
* Appointment conflict prevention

---

## 👨‍⚕️ Doctor Management

* Doctor profiles
* Department association
* Polyclinic association
* Doctor schedules
* Doctor leave management
* Appointment management

---

## 🧑‍🦽 Patient Management

* Patient profiles
* Patient information management
* Appointment history
* Examination history
* Medical record access according to authorization rules

---

## 🩺 Examination Management

* Examination records
* Patient examination history
* Diagnosis information
* Prescription information
* Doctor-controlled examination workflows

---

## 🏥 Hospital Organization

* Department management
* Polyclinic management
* Doctor-to-department relationships
* Doctor-to-polyclinic relationships

---

## 🔔 Notifications

The system contains notification functionality for application-level patient and system notifications.

---

## 🌍 Frontend

The frontend provides role-oriented interfaces for:

* Administration
* Doctors
* Patients
* Appointments
* Patient records
* Departments
* Settings
* Notifications
* Authentication

Additional frontend capabilities include:

* Responsive UI
* Turkish / English localization
* Interactive charts
* Hospital map interface
* OCR functionality through Tesseract.js

---

# 🏗 Architecture

The project currently follows a **modular monolithic architecture**.

The backend is deployed as a single Spring Boot application while its business functionality is separated into domain modules.

```text
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT (Browser)                             │
│                   Next.js 16 + React 19 SPA                        │
│         ┌──────────┬──────────┬─────────────────────┐              │
│         │  Admin   │  Doctor  │     Patient          │              │
│         │  Panel   │ Dashboard│     Portal           │              │
│         └────┬─────┴────┬─────┴─────────┬───────────┘              │
└──────────────┼──────────┼───────────────┼──────────────────────────┘
               │   REST API (JSON)        │
               ▼          ▼               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT 3.3.5 BACKEND                        │
│                                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐        │
│  │  Security    │  │  Web Layer   │  │  AI Service        │        │
│  │  ─────────── │  │  ──────────  │  │  ────────────────  │        │
│  │  JWT Auth    │  │  Controllers │  │  Gemini API Client │        │
│  │  RBAC        │  │  DTOs        │  │  Symptom Analyzer  │        │
│  │  Rate Limit  │  │  Validation  │  │  Dept. Router      │        │
│  └──────┬──────┘  └──────┬───────┘  └──────┬─────────────┘        │
│         │                │                  │                       │
│  ┌──────▼────────────────▼──────────────────▼─────────────┐        │
│  │                  Service Layer                          │        │
│  │  Appointment · Doctor · Patient · Examination           │        │
│  │  Department · Polyclinic · Notification · DoctorLeave   │        │
│  └─────────────────────┬──────────────────────────────────┘        │
│                        │                                            │
│  ┌─────────────────────▼──────────────────────────────────┐        │
│  │              Data Access Layer (Spring Data JPA)        │        │
│  │              Flyway Migrations · Hibernate ORM          │        │
│  └─────────────────────┬──────────────────────────────────┘        │
└────────────────────────┼───────────────────────────────────────────┘
                         │
               ┌──────────▼──────────┐
               │       MySQL 8       │
               │ (Database Backend)  │
               └─────────────────────┘
```

---

# 🧩 Backend Architecture

Backend domains are separated into modules.

```text
src/main/java/com/hospital/appointmentsystem/

├── appointment/
├── doctor/
├── patient/
├── examination/
├── department/
├── polyclinic/
├── notification/
├── doctorleave/
├── user/
├── setting/
├── security/
├── config/
└── exception/
```

Most domain modules follow a structure similar to:

```text
module/
├── api/
├── impl/
└── web/
```

Where applicable:

* `api/` contains DTOs and contracts
* `impl/` contains domain implementation and persistence logic
* `web/` contains REST controllers

This structure keeps controllers, business logic, and persistence concerns separated.

---

# 🛠 Technology Stack

## Backend

| Technology           | Version | Purpose                          |
| -------------------- | ------: | -------------------------------- |
| Java                 |      17 | Backend language                 |
| Spring Boot          |   3.3.5 | Application framework            |
| Spring Security      |     6.x | Security and authorization       |
| Spring Data JPA      |     3.x | Data access                      |
| Hibernate            |     6.x | ORM                              |
| Flyway               |    10.x | Database migrations              |
| JJWT                 |  0.11.5 | JWT handling                     |
| Bucket4J             |  8.10.1 | Rate limiting                    |
| Micrometer           |       — | Application metrics              |
| Spring Boot Actuator |       — | Health and operational endpoints |
| Maven                |    3.9+ | Build and dependency management  |

## Frontend

| Technology   | Version | Purpose            |
| ------------ | ------: | ------------------ |
| Next.js      |      16 | Frontend framework |
| React        |      19 | UI library         |
| Recharts     |     3.9 | Charts             |
| Tesseract.js |     7.0 | OCR                |
| CSS Modules  |       — | Component styling  |

## Infrastructure

| Technology     | Purpose                           |
| -------------- | --------------------------------- |
| Docker         | Containerization                  |
| GitHub Actions | Continuous integration            |
| MySQL 8        | Primary database (local / production) |

---

# 📁 Project Structure

```text
hospital-management-system/
│
├── .github/
│   └── workflows/
│       └── build.yml
│
├── docs/
│   ├── PROJECT_MASTER_PLAN.md
│   ├── FRONTEND-UX-MAP.md
│   └── DATABASE-MIGRATION-*.md
│
├── frontend/
│   └── src/
│       ├── app/
│       │   ├── admin/
│       │   ├── doctor/
│       │   ├── appointments/
│       │   ├── book-appointment/
│       │   ├── departments/
│       │   ├── patients/
│       │   ├── patient-records/
│       │   ├── patient-notifications/
│       │   ├── settings/
│       │   ├── login/
│       │   ├── register/
│       │   └── forgot-password/
│       │
│       ├── components/
│       ├── context/
│       ├── hooks/
│       ├── services/
│       ├── locales/
│       └── utils/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hospital/appointmentsystem/
│   │   │       ├── appointment/
│   │   │       ├── doctor/
│   │   │       ├── patient/
│   │   │       ├── examination/
│   │   │       ├── department/
│   │   │       ├── polyclinic/
│   │   │       ├── notification/
│   │   │       ├── doctorleave/
│   │   │       ├── user/
│   │   │       ├── setting/
│   │   │       ├── security/
│   │   │       ├── config/
│   │   │       └── exception/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/
│   │           └── migration/
│   │               └── mysql/
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

# 🚀 Getting Started

## 🔑 Credentials (Development Only)

> [!WARNING]  
> The application does **not** have a hardcoded default admin password. You MUST provide an `ADMIN_PASSWORD` environment variable to start the application.

If the application is started in a non-production profile (e.g. `dev`), it will provision test users for local development:

| Role | Username (TC) | Password |
|---|---|---|
| **Admin** | `admin` | *(Set by `ADMIN_PASSWORD` env variable)* |
| **Test Doktor** | `88888888888` | `local_test_secret_2026` (configurable via `TEST_USER_PASSWORD`) |
| **Test Hasta** | `99999999999` | `local_test_secret_2026` (configurable via `TEST_USER_PASSWORD`) |


## Prerequisites

Install the following:

* Java 17+
* Maven 3.9+
* Node.js
* npm
* MySQL 8
* Git

### Infrastructure
| Technology | Purpose |
|---|---|
| **Docker** | Multi-stage containerized builds |
| **GitHub Actions** | CI/CD pipeline |
| **MySQL 8** | Primary database (local / production) |
| **Aiven** | Cloud managed MySQL database |

---

## 1. Clone

```bash
git clone https://github.com/akifkeklik/hospital-management-system.git
cd hospital-management-system
```

---

## 2. Create a Local Database

For local development:

```sql
CREATE DATABASE hospitaldb;
```

Make sure the database server is running.

---

## 3. Configure the Backend

Set the required environment variables (e.g. in your shell or `.env` file).

Example configuration:

```bash
export ADMIN_PASSWORD="your-local-admin-password"
export JWT_SECRET="your-local-jwt-secret"

export DB_URL="jdbc:mysql://localhost:3306/hospitaldb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
export DB_USERNAME="your-local-user"
export DB_PASSWORD="your-local-password"
```

Do not commit real secrets to Git.

---

## 4. Start the Backend

```bash
mvn clean compile
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

## 5. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:3000
```

---

## 6. Security on First Run

When running the application for the first time, it expects the `ADMIN_PASSWORD` variable.

* **Admin User:** Created automatically using the `ADMIN_PASSWORD` provided.
* **Test Users:** `88888888888` (Doctor) and `99999999999` (Patient) are provisioned with the password `local_test_secret_2026` (only if the `dev` or `test` profile is active).

---

# ⚙️ Configuration

The application is configured primarily through environment variables.

| Variable                   | Required | Description                        |
| -------------------------- | -------- | ---------------------------------- |
| `ADMIN_PASSWORD`           | Yes      | The password for the default admin |
| `JWT_SECRET`               | Yes      | Secret used to sign JWT tokens     |
| `DB_URL`                   | No       | JDBC database connection           |
| `DB_DRIVER`                | No       | JDBC driver                        |
| `DB_USERNAME`              | No       | Database username                  |
| `DB_PASSWORD`              | No       | Database password                  |
| `DDL_AUTO`                 | No       | Hibernate schema strategy          |
| `SHOW_SQL`                 | No       | Enable SQL logging                 |
| `CORS_ALLOWED_ORIGINS`     | No       | Allowed frontend origins           |
| `RATE_LIMIT_AUTH_CAPACITY` | No       | Authentication rate-limit capacity |
| `RATE_LIMIT_AUTH_MINUTES`  | No       | Authentication rate-limit window   |

Defaults may differ between local and production environments.

Production credentials should always be supplied through the deployment platform's secret/environment configuration.

---

# 📡 API

The backend exposes REST endpoints under `/api`.

## Authentication

| Method | Endpoint                    | Description                |
| ------ | --------------------------- | -------------------------- |
| POST   | `/api/auth/register`        | Register patient           |
| POST   | `/api/auth/doctor-register` | Register doctor            |
| POST   | `/api/auth/login`           | Authenticate user          |
| POST   | `/api/auth/logout`          | Clear auth cookie          |
| POST   | `/api/auth/forgot-password` | Initiate password recovery |
| POST   | `/api/auth/reset-password`  | Complete password recovery |
| POST   | `/api/auth/force-change-password`| Required password change|
| GET    | `/api/auth/me`              | Current authenticated user |

## Appointments

| Method | Endpoint                         | Description          |
| ------ | -------------------------------- | -------------------- |
| GET    | `/api/appointments`              | List appointments    |
| POST   | `/api/appointments`              | Create appointment   |
| PUT    | `/api/appointments/{id}`         | Update appointment   |
| DELETE | `/api/appointments/{id}`         | Cancel appointment   |
| GET    | `/api/appointments/doctor/{id}`  | Doctor appointments  |
| GET    | `/api/appointments/patient/{id}` | Patient appointments |

## Doctors

| Method | Endpoint                            | Description          |
| ------ | ----------------------------------- | -------------------- |
| GET    | `/api/doctors`                      | List doctors         |
| GET    | `/api/doctors/{id}`                 | Doctor details       |
| GET    | `/api/doctors/{id}/available-slots` | Available time slots |

## Patients

| Method | Endpoint             | Description     |
| ------ | -------------------- | --------------- |
| GET    | `/api/patients`      | List patients   |
| GET    | `/api/patients/{id}` | Patient details |
| PUT    | `/api/patients/{id}` | Update patient  |

## Examinations

| Method | Endpoint                         | Description                 |
| ------ | -------------------------------- | --------------------------- |
| GET    | `/api/examinations`              | List examinations           |
| POST   | `/api/examinations`              | Create examination          |
| GET    | `/api/examinations/patient/{id}` | Patient examination history |

## Departments & Polyclinics

| Method | Endpoint           | Description      |
| ------ | ------------------ | ---------------- |
| GET    | `/api/departments` | List departments |
| GET    | `/api/polyclinics` | List polyclinics |

## System

| Method | Endpoint               | Description         |
| ------ | ---------------------- | ------------------- |
| GET    | `/actuator/health`     | Application health  |
| GET    | `/actuator/prometheus` | Application metrics |

> The exact authorization requirements of individual endpoints are enforced by the backend security configuration.

---

# 🔐 Authentication & Security

Security is implemented with Spring Security and JWT-based authentication.

## Authentication Flow

```text
Client
  │
  │ POST /api/auth/login
  ▼
Authentication Controller
  │
  ├── Validate request
  ├── Find user
  ├── Verify BCrypt password
  └── Generate JWT
  │
  ▼
Client receives JWT
  │
  │ Authorization: Bearer <token>
  ▼
JWT Authentication Filter
  │
  ├── Validate token
  ├── Extract user identity
  └── Establish SecurityContext
  │
  ▼
Authorization
  │
  ├── Role checks
  └── Endpoint permissions
  │
  ▼
Controller → Service → Repository
```

## Security Controls

### JWT Authentication

The backend uses stateless JWT authentication.

### Role-Based Authorization

The main application roles are:

```text
ADMIN
DOCTOR
PATIENT
```

### Password Security

Passwords are stored using BCrypt hashing rather than plaintext storage.

### Rate Limiting

Authentication endpoints are protected by Bucket4J-based rate limiting to reduce brute-force attempts.

### CORS

Cross-origin requests are controlled through backend CORS configuration.

### Request Validation

Incoming request data is validated before entering the business layer.

### Request Tracing

Requests receive trace identifiers through the request logging infrastructure, making application logs easier to correlate.

---

# 🗄 Database

The application uses relational database persistence through:

* Spring Data JPA
* Hibernate
* Flyway

## Database Support

The system uses **MySQL 8** as its primary database:

| Environment | Database | Driver |
|---|---|---|
| Local Development | MySQL 8 | `com.mysql.cj.jdbc.Driver` |
| Cloud (Aiven) | MySQL 8 | `com.mysql.cj.jdbc.Driver` |

### Schema Migration

Database versioning is managed by **Flyway** with vendor-specific migration scripts:

```text
src/main/resources/db/migration/
└── mysql/
    └── V1__init_schema.sql       # MySQL-specific DDL
```

Flyway automatically applies the migration scripts on startup.

## Main Domain Relationships

```text
User
 ├── Doctor
 │     ├── Department
 │     ├── Polyclinic
 │     ├── Appointments
 │     └── Doctor Leave
 │
 └── Patient
       ├── Appointments
       ├── Examinations
       └── Notifications
```

---

# ⚡ Caching

The backend includes Spring Cache support for selected read-heavy operations.

The current approach uses application-level caching without requiring an external cache service.

This keeps the deployment architecture relatively simple while providing a path for future optimization.

An external cache such as Redis can be considered later if actual production workload requires it.

---

# 📊 Observability

The backend includes basic application observability through Spring Boot Actuator and Micrometer.

## Health Check

```text
GET /actuator/health
```

## Prometheus Metrics

```text
GET /actuator/prometheus
```

## Request Logging

Requests are logged with correlation information using MDC-based trace IDs.

This makes it easier to follow a request across:

```text
HTTP Request
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

---

# 🧪 Testing

The project contains tests covering multiple areas of the application.

| Test Area                   | Technology            |
| --------------------------- | --------------------- |
| Unit testing                | JUnit 5               |
| Mocking                     | Mockito               |
| Spring integration testing  | Spring Boot Test      |
| Security testing            | Spring Security Test  |
| Test database               | H2                    |
| Database performance checks | Custom test utilities |

## Run All Tests

```bash
mvn test
```

## Run a Specific Test

```bash
mvn test -Dtest=AppointmentServiceTest
```

## SQL Debugging

```bash
mvn test -Dspring.jpa.show-sql=true
```

### Performance Testing

The project includes custom database performance analyzers:

- **`NPlusOneBaselineTest`** — Detects N+1 query problems
- **`DatabaseIndexAnalyzer`** — Analyzes index usage efficiency
- **`DatabaseIndexVerifier`** — Verifies required indexes exist

These are used to identify inefficient query patterns and verify expected database indexes.

---

# 🐳 Docker

The project includes a `Dockerfile` for containerized backend deployment. It uses a multi-stage build process to ensure a lightweight production image.

**Build:**

```bash
docker build -t hospital-management-system .
```

**Run:**

```bash
docker run -p 8080:8080 \
  -e JWT_SECRET="your-secret" \
  -e DB_URL="jdbc:mysql://host:3306/hospitaldb" \
  -e DB_DRIVER="com.mysql.cj.jdbc.Driver" \
  -e DB_USERNAME="your-user" \
  -e DB_PASSWORD="your-password" \
  hospital-management-system
```

*Note: For production, secrets should be provided securely through the deployment environment (e.g., Render Environment Variables) rather than hardcoded.*

---

# 🔄 CI/CD

**GitHub Actions** is used for continuous integration, validating both backend and frontend environments automatically on every push and pull request.

```text
Git Push / Pull Request
          │
          ▼
    GitHub Actions
          │
     ┌────┴─────┐
     ▼          ▼
 Backend     Frontend
     │          │
     ▼          ▼
 Maven       npm
 Build       Build
 Tests
     │          │
     └────┬─────┘
          ▼
      CI Result
```

Workflow configuration is located at: `.github/workflows/build.yml`

---

# ☁️ Deployment

The application is production-ready and fully supports modern cloud deployment platforms. The current production architecture securely separates the frontend, backend, and database layers.

### Production Architecture

```text
┌───────────────────────────────┐
│           Frontend            │
│       Next.js (Vercel)        │
└───────────────┬───────────────┘
                │ HTTPS (REST API)
                ▼
┌───────────────────────────────┐
│            Backend            │
│      Spring Boot (Render)     │
└───────────────┬───────────────┘
                │ TCP (JDBC/SSL)
                ▼
┌───────────────────────────────┐
│           Database            │
│       MySQL 8.4 (Aiven)       │
└───────────────────────────────┘
```

### 1. Database Deployment (Aiven)
- The application uses **Aiven MySQL** for the production database.
- Database schemas and initial data are automatically managed and migrated by **Flyway** on application startup.

### 2. Backend Deployment (Render)
- The Spring Boot backend is deployed as a Web Service on **Render**.
- Environment variables (`JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`) must be configured in the Render Dashboard.
- Application health is automatically monitored via Spring Boot Actuator endpoints.

### 3. Frontend Deployment (Vercel)
- The Next.js frontend is deployed on **Vercel**.
- ⚠️ **CRITICAL:** The `Root Directory` MUST be set to `frontend` in the Vercel Project Settings (`Settings -> General -> Root Directory`) for Next.js to be correctly detected and built. Do not use a `vercel.json` file for this.
- The `NEXT_PUBLIC_API_URL` environment variable must be configured in Vercel to point to the live Render backend URL.


---

# 🔐 Password Reset

The application implements a secure, token-based password reset mechanism using **Resend** as the email provider.

### Architecture
- **Token Generation**: Securely generated 32-byte tokens (`SecureRandom`).
- **Storage**: Only the SHA-256 hash of the token is stored in the database to prevent token leakage from database dumps.
- **Expiration**: Tokens expire after 30 minutes (configurable).
- **Single-Use**: Tokens are invalidated immediately after a successful reset.
- **Security**: 
  - Prevents User Enumeration on the `/forgot-password` endpoint.
  - Implements Rate Limiting (bypassing CORS OPTIONS).
  - Enforces strong password policies (BCrypt hashing).

### Production Configuration (Resend Integration)

To enable email delivery in production, you must configure the following environment variables in your deployment environment (e.g., Render):

| Environment Variable | Description | Example Value |
| -------------------- | ----------- | ------------- |
| `RESEND_API_KEY` | Your Resend API Key | `re_123456789` |
| `PASSWORD_RESET_EMAIL_FROM` | Sender address (must be verified in Resend) | `no-reply@yourdomain.com` |
| `PASSWORD_RESET_EMAIL_ENABLED` | Feature toggle for sending emails | `true` |
| `PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES` | Token validity duration | `30` |
| `FRONTEND_BASE_URL` | Base URL of the frontend for reset links | `https://your-frontend-domain.vercel.app` |

**⚠️ Important Setup Steps for Resend:**
1. **Domain Verification**: You must add and verify your sending domain (e.g., `yourdomain.com`) in your [Resend Dashboard](https://resend.com/domains) by adding the provided DNS records.
2. **Sender Address**: Ensure the `MAIL_FROM` address matches the verified domain.
3. If you don't verify a domain, you can only send emails to the address associated with your Resend account (using `onboarding@resend.dev` as the sender) for testing purposes.

---

# 🧭 Development Notes

This project is intentionally being developed incrementally.

Current engineering priorities include:

* Production database configuration
* Production deployment
* End-to-end environment verification
* Observability verification
* Security validation
* Application reliability
* Deployment reproducibility

The project favors **simple and maintainable architecture** over unnecessary infrastructure complexity.

For example, external infrastructure such as Redis or a message broker should only be introduced when there is a demonstrated requirement for it.

---

# 🤝 Contributing

Contributions are welcome.

## Development Workflow

Create a feature branch:

```bash
git checkout -b feature/my-feature
```

Make your changes and run the test suite:

```bash
mvn test
```

Commit using Conventional Commits:

```bash
git commit -m "feat: add appointment validation"
```

Push:

```bash
git push origin feature/my-feature
```

Then open a Pull Request.

## Commit Convention

| Prefix      | Meaning          |
| ----------- | ---------------- |
| `feat:`     | New feature      |
| `fix:`      | Bug fix          |
| `refactor:` | Code refactoring |
| `test:`     | Tests            |
| `docs:`     | Documentation    |
| `chore:`    | Maintenance      |

---

# 📄 License

This project is licensed under the **MIT License**.

See [LICENSE](LICENSE) for details.

---

<div align="center">

**Built with ❤️ by [Akif Keklik](https://github.com/akifkeklik)**

<br/>

[![GitHub](https://img.shields.io/badge/GitHub-akifkeklik-181717?style=for-the-badge\&logo=github)](https://github.com/akifkeklik)

</div>
