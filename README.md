<div align="center">

# 🏥 Hospital Management System

### **Enterprise-Grade Healthcare Management Platform**

*A full-stack healthcare platform for managing patients, doctors, appointments, examinations, departments, notifications, and AI-assisted symptom analysis — built with Spring Boot, Next.js, PostgreSQL, and a security-focused architecture.*

[![Build and Test](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml/badge.svg)](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat\&logo=openjdk\&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat\&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-000000?style=flat\&logo=next.js\&logoColor=white)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat\&logo=react\&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat\&logo=postgresql\&logoColor=white)](https://www.postgresql.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat\&logo=mysql\&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat\&logo=docker\&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat)](LICENSE)

<br/>

**Status: Production Hardening**

<br/>

[**Architecture**](#-architecture) ·
[**Getting Started**](#-quick-start) ·
[**API Reference**](#-api-reference) ·
[**Deployment**](#-deployment)

</div>

---

## 📑 Table of Contents

* [Overview](#-overview)
* [Project Status](#-project-status)
* [Key Features](#-key-features)
* [Architecture](#-architecture)
* [Request Lifecycle](#-request-lifecycle)
* [Tech Stack](#-tech-stack)
* [Quick Start](#-quick-start)
* [Project Structure](#-project-structure)
* [API Reference](#-api-reference)
* [Security](#-security)
* [AI-Assisted Features](#-ai-assisted-features)
* [Database](#-database)
* [Caching](#-caching)
* [Observability](#-observability)
* [Testing](#-testing)
* [Deployment](#-deployment)
* [CI/CD](#-cicd-pipeline)
* [Environment Variables](#-environment-variables)
* [Architecture Decisions](#-architecture-decisions)
* [Contributing](#-contributing)
* [License](#-license)

---

## 🌟 Overview

**Hospital Management System** is a full-stack healthcare management platform designed to digitize and streamline core hospital workflows.

The platform provides role-based access for **administrators, doctors, and patients**, while supporting appointment scheduling, patient management, examinations, doctor availability, notifications, departments, polyclinics, and AI-assisted symptom analysis.

The system is built around a modular Spring Boot backend and a modern Next.js frontend, with PostgreSQL as the target production database and Flyway-based schema versioning.

> **Project goal:** Build a maintainable, secure, observable, and deployable healthcare platform while applying real-world backend engineering practices such as layered architecture, RBAC, database migrations, rate limiting, automated testing, structured logging, caching, and CI/CD.

---

## 🚦 Project Status

The project is actively being hardened toward production deployment.

| Area                               | Status         |
| ---------------------------------- | -------------- |
| Backend architecture               | ✅ Implemented  |
| Frontend application               | ✅ Implemented  |
| Authentication                     | ✅ Implemented  |
| Role-based authorization           | ✅ Implemented  |
| Appointment management             | ✅ Implemented  |
| Patient management                 | ✅ Implemented  |
| Doctor management                  | ✅ Implemented  |
| Examination workflows              | ✅ Implemented  |
| Database migrations                | ✅ Implemented  |
| Security hardening                 | ✅ Implemented  |
| Rate limiting                      | ✅ Implemented  |
| Request tracing                    | ✅ Implemented  |
| Application metrics                | ✅ Implemented  |
| Automated tests                    | ✅ Implemented  |
| CI pipeline                        | ✅ Implemented  |
| Production PostgreSQL architecture | 🚧 Hardening   |
| Production deployment              | 🚧 In progress |
| Production observability           | 🚧 In progress |

> The repository documentation is intentionally aligned with the current engineering state rather than claiming production readiness before the final deployment and verification process is complete.

---

# ✨ Key Features

<table>
<tr>
<td width="50%">

### 🗓️ Appointment Management

* Appointment booking and scheduling
* Doctor availability management
* Time-slot generation
* Appointment status tracking
* Conflict and double-booking prevention
* Patient appointment history
* Doctor appointment views

</td>
<td width="50%">

### 🤖 AI-Assisted Symptom Analysis

* Google Gemini integration
* Natural-language symptom analysis
* Department recommendation
* Preliminary AI-generated suggestions
* Rule-based fallback mode
* Graceful degradation when AI services are unavailable

</td>
</tr>

<tr>
<td width="50%">

### 👨‍⚕️ Multi-Role Dashboards

* **Admin** — system administration and management
* **Doctor** — appointments, patients and examinations
* **Patient** — appointments, records and symptom analysis
* Role-aware navigation
* Role-based endpoint authorization

</td>
<td width="50%">

### 🔐 Security

* JWT-based stateless authentication
* Spring Security
* Role-based access control
* BCrypt password hashing
* Authentication rate limiting
* CORS configuration
* Request validation
* MDC-based request tracing
* Centralized exception handling

</td>
</tr>

<tr>
<td width="50%">

### 🏥 Clinical Workflows

* Patient records
* Examination records
* Medical history
* Prescription tracking
* Doctor leave management
* Department organization
* Polyclinic management
* Patient notifications

</td>
<td width="50%">

### 🌍 Frontend & UX

* Next.js App Router
* Responsive UI
* Turkish / English localization
* Interactive dashboards
* Data visualization
* Hospital map
* OCR support with Tesseract.js
* Real-time user notifications

</td>
</tr>
</table>

---

# 🏗 Architecture

The application follows a **modular monolithic architecture** with clear domain boundaries.

The backend is deployed as a single Spring Boot application while internally separating business domains into independent modules.

```text
                         ┌─────────────────────────┐
                         │        CLIENTS          │
                         │                         │
                         │     Web Browser         │
                         └────────────┬────────────┘
                                      │
                                      │ HTTPS / REST
                                      ▼
                         ┌─────────────────────────┐
                         │       NEXT.JS 16        │
                         │       React 19          │
                         │                         │
                         │  Admin / Doctor /       │
                         │  Patient Interfaces     │
                         └────────────┬────────────┘
                                      │
                                      │ REST / JSON
                                      ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         SPRING BOOT BACKEND                         │
│                                                                      │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────────────┐ │
│  │ Security     │ │ Web Layer    │ │ AI Module                   │ │
│  │              │ │              │ │                              │ │
│  │ JWT          │ │ Controllers  │ │ Gemini Integration            │ │
│  │ RBAC         │ │ DTOs         │ │ Symptom Analysis             │ │
│  │ Rate Limit   │ │ Validation   │ │ Department Recommendation     │ │
│  └──────┬───────┘ └──────┬───────┘ └──────────────┬───────────────┘ │
│         │                 │                        │                 │
│         └─────────────────┼────────────────────────┘                 │
│                           ▼                                          │
│                  ┌─────────────────────┐                             │
│                  │   SERVICE LAYER     │                             │
│                  │                     │                             │
│                  │ Appointment         │                             │
│                  │ Doctor              │                             │
│                  │ Patient             │                             │
│                  │ Examination         │                             │
│                  │ Department          │                             │
│                  │ Polyclinic          │                             │
│                  │ Notification        │                             │
│                  │ Doctor Leave        │                             │
│                  │ User                │                             │
│                  └──────────┬──────────┘                             │
│                             │                                        │
│                             ▼                                        │
│                  ┌─────────────────────┐                             │
│                  │ DATA ACCESS LAYER   │                             │
│                  │                     │                             │
│                  │ Spring Data JPA     │                             │
│                  │ Hibernate ORM       │                             │
│                  │ Flyway              │                             │
│                  └──────────┬──────────┘                             │
└─────────────────────────────┼────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │     PostgreSQL      │
                    │ Production Database │
                    └─────────────────────┘
```

### Modular Package Design

Each major domain follows a consistent layered structure:

```text
module/
├── api/          # DTOs, contracts and interfaces
├── impl/         # Services, repositories and domain implementation
└── web/          # REST controllers
```

This keeps domain logic organized while avoiding unnecessary distributed-system complexity.

---

# 🔄 Request Lifecycle

A typical authenticated API request flows through the following layers:

```text
HTTP Request
     │
     ▼
CORS / Web Configuration
     │
     ▼
Rate Limiting
     │
     ▼
JWT Authentication Filter
     │
     ▼
Spring Security Authorization
     │
     ▼
Controller
     │
     ▼
DTO Validation
     │
     ▼
Service Layer
     │
     ├──────────────► Cache
     │
     ▼
Repository / JPA
     │
     ▼
PostgreSQL
     │
     ▼
Service Response
     │
     ▼
Controller
     │
     ▼
HTTP Response
```

Cross-cutting concerns such as logging, tracing, exception handling, security, and validation are handled independently from the core domain logic.

---

# 🛠 Tech Stack

## Backend

| Technology               | Version | Purpose                        |
| ------------------------ | ------: | ------------------------------ |
| **Java**                 |  17 LTS | Core language                  |
| **Spring Boot**          |   3.3.5 | Application framework          |
| **Spring Security**      |     6.x | Authentication & authorization |
| **Spring Data JPA**      |     3.x | Data access                    |
| **Hibernate**            |     6.x | ORM                            |
| **Flyway**               |    10.x | Database migrations            |
| **JJWT**                 |  0.11.5 | JWT handling                   |
| **Bucket4J**             |  8.10.1 | Rate limiting                  |
| **Micrometer**           |       — | Metrics                        |
| **Prometheus**           |       — | Metrics collection             |
| **Spring Boot Actuator** |       — | Health and monitoring          |
| **Maven**                |    3.9+ | Build management               |

## Frontend

| Technology       | Version | Purpose                   |
| ---------------- | ------: | ------------------------- |
| **Next.js**      |      16 | Web application framework |
| **React**        |      19 | UI library                |
| **Recharts**     |     3.9 | Data visualization        |
| **Tesseract.js** |     7.0 | OCR                       |
| **CSS Modules**  |       — | Component styling         |

## Infrastructure

| Technology         | Purpose                                    |
| ------------------ | ------------------------------------------ |
| **Docker**         | Containerization                           |
| **GitHub Actions** | CI/CD                                      |
| **PostgreSQL 16**  | Production database                        |
| **MySQL 8**        | Local development / compatibility          |
| **Cloud Hosting**  | Container-compatible production deployment |

---

# 🚀 Quick Start

## Prerequisites

| Requirement | Version |
| ----------- | ------- |
| Java JDK    | 17+     |
| Maven       | 3.9+    |
| Node.js     | 18+     |
| MySQL       | 8.0+    |
| Git         | 2.x+    |

---

## 1️⃣ Clone the Repository

```bash
git clone https://github.com/akifkeklik/hospital-management-system.git
cd hospital-management-system
```

---

## 2️⃣ Database Setup

For local development, the application can use MySQL.

```sql
CREATE DATABASE IF NOT EXISTS hospitaldb;
```

Production deployments should use PostgreSQL.

---

## 3️⃣ Configure Environment Variables

Set the required environment variables:

```bash
export JWT_SECRET="your-256-bit-secret-key"
```

Example local database configuration:

```bash
export DB_URL="jdbc:mysql://localhost:3306/hospitaldb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
export DB_USERNAME="root"
export DB_PASSWORD="your-password"
```

Optional AI configuration:

```bash
export GEMINI_API_KEY="your-gemini-api-key"
```

---

## 4️⃣ Start the Backend

```bash
mvn clean install
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

---

## 5️⃣ Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:3000
```

---

## 🐳 Docker

Build the application:

```bash
docker build -t hospital-management-system .
```

Run it:

```bash
docker run -p 8080:8080 \
  -e JWT_SECRET="your-secret-key" \
  -e DB_URL="jdbc:postgresql://host:5432/hospitaldb" \
  -e DB_DRIVER="org.postgresql.Driver" \
  -e DB_USERNAME="prod_user" \
  -e DB_PASSWORD="prod_password" \
  hospital-management-system
```

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
│   │   ├── java/com/hospital/appointmentsystem/
│   │   │
│   │   ├── ai/
│   │   ├── appointment/
│   │   ├── doctor/
│   │   ├── patient/
│   │   ├── examination/
│   │   ├── department/
│   │   ├── polyclinic/
│   │   ├── notification/
│   │   ├── doctorleave/
│   │   ├── user/
│   │   ├── setting/
│   │   │
│   │   ├── security/
│   │   ├── config/
│   │   └── exception/
│   │
│   └── resources/
│       ├── application.properties
│       └── db/migration/
│           ├── mysql/
│           └── postgresql/
│
├── src/test/
├── Dockerfile
├── pom.xml
└── README.md
```

---

# 📡 API Reference

The API is organized around the main healthcare domains.

## Authentication

| Method | Endpoint                    | Description       | Auth   |
| ------ | --------------------------- | ----------------- | ------ |
| `POST` | `/api/auth/register`        | Register user     | Public |
| `POST` | `/api/auth/login`           | Authenticate user | Public |
| `POST` | `/api/auth/forgot-password` | Password recovery | Public |
| `GET`  | `/api/auth/me`              | Current user      | 🔒     |

## Appointments

| Method   | Endpoint                         | Description          | Auth       |
| -------- | -------------------------------- | -------------------- | ---------- |
| `GET`    | `/api/appointments`              | List appointments    | 🔒         |
| `POST`   | `/api/appointments`              | Create appointment   | 🔒 Patient |
| `PUT`    | `/api/appointments/{id}`         | Update appointment   | 🔒         |
| `DELETE` | `/api/appointments/{id}`         | Cancel appointment   | 🔒         |
| `GET`    | `/api/appointments/doctor/{id}`  | Doctor appointments  | 🔒 Doctor  |
| `GET`    | `/api/appointments/patient/{id}` | Patient appointments | 🔒 Patient |

## Doctors

| Method | Endpoint                            | Description     | Auth |
| ------ | ----------------------------------- | --------------- | ---- |
| `GET`  | `/api/doctors`                      | List doctors    | 🔒   |
| `GET`  | `/api/doctors/{id}`                 | Doctor details  | 🔒   |
| `GET`  | `/api/doctors/{id}/available-slots` | Available slots | 🔒   |

## Patients

| Method | Endpoint             | Description     | Auth     |
| ------ | -------------------- | --------------- | -------- |
| `GET`  | `/api/patients`      | List patients   | 🔒 Admin |
| `GET`  | `/api/patients/{id}` | Patient details | 🔒       |
| `PUT`  | `/api/patients/{id}` | Update patient  | 🔒       |

## Examinations

| Method | Endpoint                         | Description        | Auth      |
| ------ | -------------------------------- | ------------------ | --------- |
| `GET`  | `/api/examinations`              | List examinations  | 🔒        |
| `POST` | `/api/examinations`              | Create examination | 🔒 Doctor |
| `GET`  | `/api/examinations/patient/{id}` | Patient history    | 🔒        |

## Departments & Polyclinics

| Method | Endpoint           | Description      | Auth |
| ------ | ------------------ | ---------------- | ---- |
| `GET`  | `/api/departments` | List departments | 🔒   |
| `GET`  | `/api/polyclinics` | List polyclinics | 🔒   |

## AI

| Method | Endpoint                   | Description      | Auth |
| ------ | -------------------------- | ---------------- | ---- |
| `POST` | `/api/ai/analyze-symptoms` | Analyze symptoms | 🔒   |

## System

| Method | Endpoint               | Description        | Auth   |
| ------ | ---------------------- | ------------------ | ------ |
| `GET`  | `/actuator/health`     | Application health | Public |
| `GET`  | `/actuator/prometheus` | Prometheus metrics | Public |

> Endpoint availability and authorization should always be verified against the current backend implementation.

---

# 🔐 Security

Security is implemented as a layered architecture rather than a single authentication mechanism.

## Authentication Flow

```text
┌──────────────┐
│    Client    │
└──────┬───────┘
       │
       │ POST /api/auth/login
       ▼
┌────────────────────┐
│ Authentication     │
│                    │
│ ✓ Validate input   │
│ ✓ Verify password  │
│ ✓ BCrypt check     │
│ ✓ Generate JWT     │
└─────────┬──────────┘
          │
          │ JWT
          ▼
┌────────────────────┐
│ Authenticated API  │
│ Request            │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ JWT Filter         │
│                    │
│ ✓ Verify token     │
│ ✓ Extract identity │
│ ✓ Set security ctx │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ Authorization      │
│                    │
│ ✓ Role checks      │
│ ✓ Endpoint rules   │
└─────────┬──────────┘
          │
          ▼
       Response
```

## Security Layers

| Layer              | Implementation              |
| ------------------ | --------------------------- |
| Authentication     | JWT                         |
| Authorization      | Spring Security RBAC        |
| Password Storage   | BCrypt                      |
| Rate Limiting      | Bucket4J                    |
| Request Tracing    | MDC                         |
| CORS               | Spring Web Configuration    |
| Input Validation   | Bean Validation             |
| Exception Handling | Centralized exception layer |

### Supported Roles

```text
ADMIN
DOCTOR
PATIENT
```

---

# 🤖 AI-Assisted Features

The platform can integrate with Google's Gemini API for AI-assisted symptom analysis.

## Symptom Analysis

Users can describe symptoms using natural language.

The AI module can provide:

* Symptom interpretation
* Possible condition categories
* Department recommendation
* Preliminary suggestions
* Confidence information where supported

### Dual-Mode Architecture

```text
                 ┌──────────────────────┐
                 │ GEMINI_API_KEY set?  │
                 └──────────┬───────────┘
                            │
                 ┌──────────┴───────────┐
                 │                      │
                YES                    NO
                 │                      │
                 ▼                      ▼
          ┌──────────────┐       ┌──────────────┐
          │ Gemini Mode  │       │ Fallback Mode │
          │              │       │              │
          │ AI analysis  │       │ Rule-based   │
          │ NLP          │       │ matching     │
          │ Suggestions  │       │ Department   │
          └──────────────┘       └──────────────┘
```

> **Important:** AI output is intended for assistance and routing purposes only. It is **not a medical diagnosis** and should not replace evaluation by a qualified healthcare professional.

### Data Handling

Patient records remain stored in the application's configured database.

When Gemini integration is enabled, symptom input may be transmitted to the configured AI provider for processing. The application does not intentionally persist AI requests as an external patient record.

---

# 🗄 Database

## Production Database

**PostgreSQL 16** is the target production database.

## Local Development

**MySQL 8** is supported for local development and database compatibility.

| Environment       | Database      |
| ----------------- | ------------- |
| Local Development | MySQL 8       |
| Production        | PostgreSQL 16 |

## Migration Strategy

Database schema changes are version-controlled through Flyway.

```text
src/main/resources/db/migration/

├── mysql/
│   └── V1__init_schema.sql
│
└── postgresql/
    └── V1__init_schema.sql
```

The migration structure allows vendor-specific SQL while keeping database evolution version-controlled.

## Key Domain Entities

```text
                   ┌──────────────┐
                   │     User     │
                   └──────┬───────┘
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
       ┌─────────────┐         ┌─────────────┐
       │   Doctor    │         │   Patient   │
       └──────┬──────┘         └──────┬──────┘
              │                       │
              └──────────┬────────────┘
                         ▼
                 ┌──────────────┐
                 │ Appointment  │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │ Examination  │
                 └──────────────┘
```

---

# ⚡ Caching

The backend includes application-level caching for read-heavy operations where appropriate.

The current caching approach is designed to:

* Reduce repeated database reads
* Improve response latency
* Avoid unnecessary infrastructure dependencies
* Preserve application-level data consistency

Caching is implemented through Spring's caching abstraction.

The cache strategy can later be replaced or extended with an external cache such as Redis if production workload requirements justify it.

---

# 📊 Observability

The application includes several observability capabilities.

## Health

Spring Boot Actuator provides application health information.

```text
GET /actuator/health
```

## Metrics

Micrometer exposes application metrics for Prometheus-compatible monitoring.

```text
GET /actuator/prometheus
```

## Request Tracing

Requests are assigned trace identifiers through MDC-based request logging.

This enables correlation of:

```text
Request
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Error / Response
```

within application logs.

## Logging

The backend uses structured request logging and centralized exception handling to improve debugging and operational visibility.

---

# 🧪 Testing

Testing is organized across multiple layers.

| Type              | Technology           | Purpose                        |
| ----------------- | -------------------- | ------------------------------ |
| Unit Tests        | JUnit 5 + Mockito    | Business logic                 |
| Integration Tests | Spring Boot Test     | Application integration        |
| Security Tests    | Spring Security Test | Authentication / authorization |
| Performance Tests | Custom analyzers     | Query and index analysis       |
| Test Database     | H2                   | Isolated test execution        |

## Running Tests

Run all tests:

```bash
mvn test
```

Run with SQL logging:

```bash
mvn test -Dspring.jpa.show-sql=true
```

Run a specific test:

```bash
mvn test -Dtest=AppointmentServiceTest
```

## Performance Analysis

The project contains custom tooling for database performance analysis:

* `NPlusOneBaselineTest`
* `DatabaseIndexAnalyzer`
* `DatabaseIndexVerifier`

These tools help identify inefficient query patterns and verify required database indexes.

---

# ☁️ Deployment

The application is designed to be **cloud-provider independent**.

The production architecture consists of:

```text
┌─────────────────────┐
│       Vercel        │
│     Next.js App     │
└──────────┬──────────┘
           │ HTTPS
           ▼
┌─────────────────────┐
│   Spring Boot API   │
│      Container      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│     PostgreSQL      │
│   Production DB     │
└─────────────────────┘
```

The backend can be deployed to any infrastructure capable of running the application container.

### Production Requirements

* Java 17 compatible runtime
* PostgreSQL 16
* Secure environment variable management
* HTTPS
* Correct CORS configuration
* Strong JWT secret
* Database migration execution
* Health monitoring
* Application logging
* Resource and connection limits

> The deployment configuration is intentionally kept separate from the application architecture so the system is not coupled to a single cloud provider.

---

# 🔄 CI/CD Pipeline

GitHub Actions provides continuous integration.

```text
Push / Pull Request
        │
        ▼
┌───────────────────────┐
│ GitHub Actions        │
└───────────┬───────────┘
            │
       ┌────┴─────┐
       ▼          ▼
┌────────────┐ ┌──────────────┐
│  Backend   │ │   Frontend   │
│            │ │              │
│ JDK 17     │ │ Node.js      │
│ Maven      │ │ npm          │
│ Tests      │ │ Build        │
└────────────┘ └──────────────┘
       │          │
       └────┬─────┘
            ▼
      Build Result
```

The pipeline validates the backend and frontend before changes are considered ready for deployment.

---

# ⚙️ Environment Variables

| Variable                   | Required | Default                 | Description                 |
| -------------------------- | -------- | ----------------------- | --------------------------- |
| `JWT_SECRET`               | ✅        | —                       | Secret used for JWT signing |
| `DB_URL`                   | ❌        | Local MySQL URL         | JDBC connection URL         |
| `DB_DRIVER`                | ❌        | MySQL driver            | Database driver             |
| `DB_USERNAME`              | ❌        | `root`                  | Database username           |
| `DB_PASSWORD`              | ❌        | —                       | Database password           |
| `DDL_AUTO`                 | ❌        | `validate`              | Hibernate schema strategy   |
| `SHOW_SQL`                 | ❌        | `false`                 | Enable SQL logging          |
| `GEMINI_API_KEY`           | ❌        | —                       | Gemini API key              |
| `GEMINI_API_URL`           | ❌        | Configured endpoint     | Gemini API endpoint         |
| `CORS_ALLOWED_ORIGINS`     | ❌        | `http://localhost:3000` | Allowed frontend origins    |
| `RATE_LIMIT_AUTH_CAPACITY` | ❌        | `10`                    | Auth request capacity       |
| `RATE_LIMIT_AUTH_MINUTES`  | ❌        | `1`                     | Rate-limit window           |

### Production Security

Production secrets should **never** be committed to Git.

Use the secret management facilities provided by the deployment environment.

---

# 🧠 Architecture Decisions

## Why Spring Boot?

Spring Boot provides mature support for:

* REST APIs
* Security
* Dependency injection
* Database access
* Validation
* Observability
* Production operations

## Why a Modular Monolith?

The project benefits from strong domain boundaries without introducing unnecessary distributed-system complexity.

This gives the system:

* Clear module ownership
* Easier local development
* Simpler deployment
* Lower operational complexity
* A migration path toward services if future scale requires it

## Why PostgreSQL?

PostgreSQL is the production database because it provides:

* Strong transactional guarantees
* Mature indexing
* Reliable relational modeling
* Excellent Spring ecosystem support
* Strong production deployment support

## Why Flyway?

Database changes should be version-controlled alongside application code.

Flyway provides:

* Repeatable deployment
* Migration history
* Controlled schema evolution
* Environment consistency

## Why JWT?

JWT enables stateless authentication suitable for a separately deployed frontend and backend architecture.

## Why Docker?

Containerization provides a consistent runtime between development, CI, and production environments.

---

# 🤝 Contributing

Contributions are welcome.

## Development Flow

```bash
git checkout -b feature/amazing-feature
```

Make your changes, run tests, then commit:

```bash
git commit -m "feat: add amazing feature"
```

Push the branch:

```bash
git push origin feature/amazing-feature
```

Then open a Pull Request.

## Commit Convention

The project follows Conventional Commits.

| Prefix      | Description      |
| ----------- | ---------------- |
| `feat:`     | New feature      |
| `fix:`      | Bug fix          |
| `docs:`     | Documentation    |
| `refactor:` | Code refactoring |
| `test:`     | Tests            |
| `chore:`    | Maintenance      |

---

# 📄 License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ by [Akif Keklik](https://github.com/akifkeklik)**

<br/>

<sub>⭐ Star this repository if you found it useful!</sub>

<br/>

[![GitHub](https://img.shields.io/badge/GitHub-akifkeklik-181717?style=for-the-badge\&logo=github)](https://github.com/akifkeklik)

</div>
