<div align="center">

# 🏥 Hospital Management System

### Full-Stack Hospital Management Platform

A full-stack hospital management application for managing users, doctors, patients, appointments, examinations, departments, notifications, and doctor schedules.

Built with **Spring Boot**, **Next.js**, **React**, and relational database technologies, with a focus on clean backend architecture, authentication, authorization, database migrations, testing, and production-oriented engineering practices.

<br/>

[![Build and Test](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml/badge.svg)](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat\&logo=openjdk\&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat\&logo=spring-boot\&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-000000?style=flat\&logo=next.js\&logoColor=white)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat\&logo=react\&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat\&logo=postgresql\&logoColor=white)](https://www.postgresql.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat\&logo=mysql\&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat\&logo=docker\&logoColor=white)](https://www.docker.com/)
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
                         ┌───────────────────────┐
                         │       Browser         │
                         └───────────┬───────────┘
                                     │
                                     │ HTTP / REST
                                     ▼
                         ┌───────────────────────┐
                         │     Next.js 16        │
                         │      React 19         │
                         └───────────┬───────────┘
                                     │
                                     │ JSON API
                                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                     │
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌────────────────┐ │
│  │   Security   │   │ Controllers  │   │  Validation    │ │
│  │              │   │              │   │                │ │
│  │ JWT / RBAC   │   │ REST API     │   │ DTO Validation │ │
│  │ Rate Limit   │   │              │   │                │ │
│  └──────┬───────┘   └──────┬───────┘   └───────┬────────┘ │
│         │                   │                   │          │
│         └───────────────────┼───────────────────┘          │
│                             ▼                              │
│                    ┌────────────────┐                      │
│                    │ Service Layer  │                      │
│                    └───────┬────────┘                      │
│                            │                               │
│                            ▼                               │
│                    ┌────────────────┐                      │
│                    │ Repository /   │                      │
│                    │ JPA / Hibernate│                      │
│                    └───────┬────────┘                      │
└────────────────────────────┼────────────────────────────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │ Relational DB    │
                    │ PostgreSQL /     │
                    │ MySQL            │
                    └──────────────────┘
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
| PostgreSQL     | Production database target        |
| MySQL          | Local development / compatibility |

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
│   │               ├── mysql/
│   │               └── postgresql/
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

# 🚀 Getting Started

## Prerequisites

Install the following:

* Java 17+
* Maven 3.9+
* Node.js
* npm
* MySQL 8 for local development
* Git

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

Set the required environment variables.

Example:

```bash
export JWT_SECRET="replace-with-a-strong-secret"

export DB_URL="jdbc:mysql://localhost:3306/hospitaldb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
export DB_USERNAME="root"
export DB_PASSWORD="your-local-password"
```

Do not commit real secrets to Git.

---

## 4. Start the Backend

```bash
mvn clean install
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

# ⚙️ Configuration

The application is configured primarily through environment variables.

| Variable                   | Required | Description                        |
| -------------------------- | -------- | ---------------------------------- |
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
| POST   | `/api/auth/register`        | Register user              |
| POST   | `/api/auth/login`           | Authenticate user          |
| POST   | `/api/auth/forgot-password` | Password recovery          |
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

| Environment       | Database      |
| ----------------- | ------------- |
| Local development | MySQL 8       |
| Production target | PostgreSQL 16 |

The project maintains database-specific Flyway migrations:

```text
src/main/resources/db/migration/

├── mysql/
│   └── V1__init_schema.sql
│
└── postgresql/
    └── V1__init_schema.sql
```

This allows the application to maintain vendor-specific SQL while keeping schema changes version-controlled.

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

## Database Performance Tests

The repository includes tools/tests for investigating database performance, including:

```text
NPlusOneBaselineTest
DatabaseIndexAnalyzer
DatabaseIndexVerifier
```

These are used to identify inefficient query patterns and verify expected database indexes.

---

# 🐳 Docker

The project includes a Dockerfile for containerized backend deployment.

Build:

```bash
docker build -t hospital-management-system .
```

Run:

```bash
docker run -p 8080:8080 \
  -e JWT_SECRET="your-secret" \
  -e DB_URL="jdbc:postgresql://host:5432/hospitaldb" \
  -e DB_DRIVER="org.postgresql.Driver" \
  -e DB_USERNAME="your-user" \
  -e DB_PASSWORD="your-password" \
  hospital-management-system
```

For production, secrets should be provided through the deployment environment rather than hardcoded in Docker commands or source files.

---

# 🔄 CI/CD

GitHub Actions is used for continuous integration.

The current pipeline validates both backend and frontend builds.

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

Workflow configuration:

```text
.github/workflows/build.yml
```

---

# ☁️ Deployment

The application is being prepared for production deployment.

The intended production architecture separates the frontend, backend, and database:

```text
┌─────────────────────┐
│      Frontend       │
│      Next.js        │
└──────────┬──────────┘
           │ HTTPS
           ▼
┌─────────────────────┐
│       Backend       │
│     Spring Boot     │
│       Docker        │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│     PostgreSQL      │
│   Production DB     │
└─────────────────────┘
```

The application is not intentionally coupled to a specific cloud provider.

The production deployment process includes:

1. Provision PostgreSQL
2. Configure production environment variables
3. Deploy the Spring Boot backend
4. Run and verify Flyway migrations
5. Deploy the Next.js frontend
6. Configure CORS
7. Verify health endpoints
8. Verify authentication
9. Verify database connectivity
10. Verify application workflows

> Production deployment should only be considered complete after the deployed application has passed functional, database, security, and health checks.

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
