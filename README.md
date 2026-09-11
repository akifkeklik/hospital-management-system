<div align="center">

# 🏥 Hospital Management System

### **Enterprise-Grade Healthcare Platform**

*A full-stack hospital management system featuring AI-powered diagnostics, real-time appointment scheduling, multi-role dashboards, and production-ready cloud deployment.*

[![Build and Test](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml/badge.svg)](https://github.com/akifkeklik/hospital-management-system/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-000000?style=flat&logo=next.js&logoColor=white)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat)](LICENSE)

<br/>

[**Live Demo**](#-deployment) · [**API Docs**](#-api-reference) · [**Getting Started**](#-quick-start) · [**Architecture**](#-architecture)

<br/>

<img src="https://img.shields.io/badge/Status-Production%20Ready-brightgreen?style=for-the-badge" alt="Production Ready"/>

</div>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [API Reference](#-api-reference)
- [Security](#-security)
- [AI Integration](#-ai-powered-features)
- [Database](#-database)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [CI/CD](#-cicd-pipeline)
- [Environment Variables](#-environment-variables)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

**Hospital Management System** is a comprehensive, enterprise-grade healthcare platform designed to digitize and streamline hospital operations. Built with a modern microservice-inspired architecture, it enables seamless management of patients, doctors, appointments, examinations, departments, and more — all backed by AI-powered diagnostic recommendations.

> **Why this project?**  
> Healthcare facilities need robust, scalable, and secure software. This system provides a full production-ready solution — from patient registration to AI-assisted symptom analysis — with role-based access, real-time notifications, and multi-language support.

---

## ✨ Key Features

<table>
<tr>
<td width="50%">

### 🗓️ Appointment Management
- Real-time appointment booking & scheduling
- Doctor availability & time-slot management
- Appointment status tracking (Scheduled → Completed)
- Conflict detection & double-booking prevention
- Patient appointment history

</td>
<td width="50%">

### 🤖 AI-Powered Diagnostics
- **Google Gemini** integration for symptom analysis
- Intelligent department routing based on symptoms
- AI-generated preliminary diagnostic suggestions
- Graceful fallback to rule-based engine when API is unavailable
- Privacy-first: no patient data stored externally

</td>
</tr>
<tr>
<td width="50%">

### 👨‍⚕️ Multi-Role Dashboards
- **Admin Panel** — Full system control, user management, analytics
- **Doctor Dashboard** — Patient queue, examination tools, schedule
- **Patient Portal** — Book appointments, view records, AI symptom checker
- Role-specific navigation, views & permissions

</td>
<td width="50%">

### 🔐 Enterprise Security
- JWT-based stateless authentication
- Role-based access control (RBAC): `ADMIN`, `DOCTOR`, `PATIENT`
- Password hashing with BCrypt
- Rate limiting on auth endpoints (Bucket4J)
- CORS configuration for cross-origin requests
- Request logging with MDC trace IDs

</td>
</tr>
<tr>
<td width="50%">

### 🏥 Clinical Features
- Patient examination records & medical history
- Doctor leave management & scheduling
- Department & polyclinic organization
- Prescription tracking in examinations
- Patient notification system

</td>
<td width="50%">

### 🌍 Internationalization & UX
- Multi-language support (TR / EN)
- Responsive design for all devices
- Real-time toast notifications
- Hospital map component
- OCR scanning via Tesseract.js
- Interactive data charts (Recharts)

</td>
</tr>
</table>

---

## 🏗 Architecture

```
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
              │  MySQL 8 / Postgres │
              │  (Dual DB Support)  │
              └─────────────────────┘
```

### Modular Package Design

Each domain module follows a **clean, layered architecture**:

```
module/
├── api/          # DTOs, interfaces, contracts
├── impl/         # Service implementations, repositories, entities
└── web/          # REST controllers
```

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| **Java** | 17 (LTS) | Core language |
| **Spring Boot** | 3.3.5 | Application framework |
| **Spring Security** | 6.x | Authentication & authorization |
| **Spring Data JPA** | 3.x | Data access & ORM |
| **Hibernate** | 6.x | JPA implementation |
| **Flyway** | 10.x | Database version control & migrations |
| **JJWT** | 0.11.5 | JSON Web Token handling |
| **Bucket4J** | 8.10.1 | Rate limiting |
| **Micrometer + Prometheus** | — | Metrics & observability |
| **Spring Boot Actuator** | — | Health checks & monitoring |
| **Maven** | 3.9+ | Build & dependency management |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| **Next.js** | 16 | React meta-framework (App Router) |
| **React** | 19 | UI library |
| **Recharts** | 3.9 | Data visualization & charts |
| **Tesseract.js** | 7.0 | OCR / document scanning |
| **CSS Modules** | — | Scoped component styling |

### Infrastructure
| Technology | Purpose |
|---|---|
| **Docker** | Multi-stage containerized builds |
| **GitHub Actions** | CI/CD pipeline |
| **MySQL 8** | Primary database (local / production) |
| **PostgreSQL 16** | Cloud database (Render deployment) |
| **Render** | Cloud hosting platform |

---

## 🚀 Quick Start

### Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Git | 2.x+ |

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/akifkeklik/hospital-management-system.git
cd hospital-management-system
```

### 2️⃣ Database Setup

```sql
-- Create the database
CREATE DATABASE IF NOT EXISTS hospitaldb;
```

### 3️⃣ Configure Environment

Create a `.env` file or set these environment variables:

```bash
# Required
export JWT_SECRET="your-256-bit-secret-key-here-min-32-chars"

# Optional (defaults shown)
export DB_URL="jdbc:mysql://localhost:3306/hospitaldb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
export DB_USERNAME="root"
export DB_PASSWORD="12345678"

# AI Features (optional — runs in demo mode without)
export GEMINI_API_KEY="your-gemini-api-key"
```

### 4️⃣ Start the Backend

```bash
# Build and run
mvn clean install
mvn spring-boot:run
```

The backend will start at **`http://localhost:8080`**

### 5️⃣ Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start at **`http://localhost:3000`**

### 🐳 Docker (Alternative)

```bash
# Build the image
docker build -t hospital-management-system .

# Run with environment variables
docker run -p 8080:8080 \
  -e JWT_SECRET="your-secret-key" \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/hospitaldb" \
  -e DB_USERNAME="root" \
  -e DB_PASSWORD="12345678" \
  hospital-management-system
```

---

## 📁 Project Structure

```
hospital-management-system/
│
├── 📂 .github/workflows/         # CI/CD pipeline configuration
│   └── build.yml                 # GitHub Actions (build + test)
│
├── 📂 docs/                      # Project documentation
│   ├── PROJECT_MASTER_PLAN.md
│   ├── FRONTEND-UX-MAP.md
│   └── DATABASE-MIGRATION-*.md
│
├── 📂 frontend/                  # Next.js 16 Frontend
│   └── src/
│       ├── app/                  # App Router pages
│       │   ├── admin/            # Admin panel pages
│       │   ├── doctor/           # Doctor dashboard
│       │   ├── appointments/     # Appointment management
│       │   ├── book-appointment/ # Booking flow
│       │   ├── departments/      # Department listing
│       │   ├── patients/         # Patient management
│       │   ├── patient-records/  # Medical records
│       │   ├── patient-notifications/
│       │   ├── settings/         # System settings
│       │   ├── login/            # Authentication
│       │   ├── register/         # Registration
│       │   └── forgot-password/  # Password recovery
│       ├── components/           # Reusable UI components
│       │   ├── Header.js         # Navigation header
│       │   ├── Sidebar.js        # Side navigation
│       │   ├── DoctorDashboard.js
│       │   ├── PatientDashboard.js
│       │   ├── SymptomAnalyzer.js # AI symptom checker
│       │   ├── HospitalMap.js    # Interactive map
│       │   ├── Scanner.js        # OCR scanner
│       │   ├── DashboardCharts.js
│       │   └── ...
│       ├── context/              # React Context providers
│       ├── hooks/                # Custom React hooks
│       ├── services/             # API service layer
│       ├── locales/              # i18n translations (TR/EN)
│       └── utils/                # Utility functions
│
├── 📂 src/main/java/com/hospital/appointmentsystem/
│   ├── 🚀 HospitalAppointmentApplication.java   # Entry point
│   │
│   ├── 📂 ai/                   # AI Module
│   │   ├── AiController.java
│   │   ├── AiService.java       # Gemini integration
│   │   └── AiDtos.java
│   │
│   ├── 📂 appointment/          # Appointment Module
│   │   ├── api/                  # DTOs & interfaces
│   │   ├── impl/                 # Service & repository
│   │   └── web/                  # REST controllers
│   │
│   ├── 📂 doctor/               # Doctor Module
│   ├── 📂 patient/              # Patient Module
│   ├── 📂 examination/          # Examination Module
│   ├── 📂 department/           # Department Module
│   ├── 📂 polyclinic/           # Polyclinic Module
│   ├── 📂 notification/         # Notification Module
│   ├── 📂 doctorleave/          # Doctor Leave Module
│   ├── 📂 user/                 # User Management Module
│   ├── 📂 setting/              # System Settings Module
│   │
│   ├── 📂 security/             # Security & Auth
│   │   ├── SecurityConfig.java  # Spring Security config
│   │   ├── JwtUtil.java         # JWT token utilities
│   │   ├── JwtAuthFilter.java   # JWT authentication filter
│   │   ├── AuthController.java  # Login/Register endpoints
│   │   ├── RateLimitFilter.java # Brute-force protection
│   │   └── SecurityService.java # Authorization service
│   │
│   ├── 📂 config/               # Application Configuration
│   │   ├── WebConfig.java       # CORS & web settings
│   │   ├── CacheConfig.java     # Caching configuration
│   │   └── RequestLoggingFilter.java
│   │
│   └── 📂 exception/            # Global exception handling
│
├── 📂 src/main/resources/
│   ├── application.properties    # App configuration
│   └── db/migration/            # Flyway migrations
│       ├── mysql/V1__init_schema.sql
│       └── postgresql/V1__init_schema.sql
│
├── 📂 src/test/                  # Test suite
│   ├── java/.../
│   │   ├── NPlusOneBaselineTest.java
│   │   ├── DatabaseIndexAnalyzer.java
│   │   ├── DatabaseIndexVerifier.java
│   │   ├── appointment/         # Appointment tests
│   │   ├── doctorleave/         # Doctor leave tests
│   │   ├── security/            # Security tests
│   │   └── observability/       # Metrics tests
│   └── resources/
│       └── application-test.properties
│
├── Dockerfile                    # Multi-stage Docker build
├── pom.xml                       # Maven configuration
└── README.md                     # You are here!
```

---

## 📡 API Reference

### Authentication

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user | Public |
| `POST` | `/api/auth/login` | Login & get JWT token | Public |
| `POST` | `/api/auth/forgot-password` | Request password reset | Public |
| `GET` | `/api/auth/me` | Get current user info | 🔒 |

### Appointments

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/appointments` | List appointments | 🔒 |
| `POST` | `/api/appointments` | Create appointment | 🔒 Patient |
| `PUT` | `/api/appointments/{id}` | Update appointment | 🔒 |
| `DELETE` | `/api/appointments/{id}` | Cancel appointment | 🔒 |
| `GET` | `/api/appointments/doctor/{id}` | Doctor's appointments | 🔒 Doctor |
| `GET` | `/api/appointments/patient/{id}` | Patient's appointments | 🔒 Patient |

### Doctors

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/doctors` | List all doctors | 🔒 |
| `GET` | `/api/doctors/{id}` | Get doctor details | 🔒 |
| `GET` | `/api/doctors/{id}/available-slots` | Get available time slots | 🔒 |

### Patients

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/patients` | List all patients | 🔒 Admin |
| `GET` | `/api/patients/{id}` | Get patient details | 🔒 |
| `PUT` | `/api/patients/{id}` | Update patient info | 🔒 |

### Examinations

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/examinations` | List examinations | 🔒 |
| `POST` | `/api/examinations` | Create examination record | 🔒 Doctor |
| `GET` | `/api/examinations/patient/{id}` | Patient's exam history | 🔒 |

### Departments & Polyclinics

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/departments` | List departments | 🔒 |
| `GET` | `/api/polyclinics` | List polyclinics | 🔒 |

### AI Features

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/ai/analyze-symptoms` | AI symptom analysis | 🔒 |

### System

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/actuator/health` | Health check | Public |
| `GET` | `/actuator/prometheus` | Prometheus metrics | Public |

---

## 🔐 Security

### Authentication Flow

```
┌──────────┐     POST /api/auth/login      ┌──────────────┐
│  Client   │ ────────────────────────────▶ │  Auth Server  │
│           │                               │               │
│           │     200 OK + JWT Token         │  ✓ Validate   │
│           │ ◀──────────────────────────── │  ✓ BCrypt     │
│           │                               │  ✓ Generate   │
│           │     GET /api/... + Bearer      │               │
│           │ ────────────────────────────▶ │  ✓ Verify JWT │
│           │                               │  ✓ Check Role │
│           │     200 OK + Response          │  ✓ Authorize  │
│           │ ◀──────────────────────────── │               │
└──────────┘                               └──────────────┘
```

### Security Layers

| Layer | Implementation | Description |
|---|---|---|
| **Authentication** | JWT (JJWT 0.11.5) | Stateless token-based auth, 10-hour expiry |
| **Authorization** | Spring Security RBAC | Role-based endpoint protection |
| **Password Storage** | BCrypt | Industry-standard hashing |
| **Rate Limiting** | Bucket4J | Configurable rate limits on auth endpoints |
| **Request Tracing** | MDC + Logging Filter | Unique trace ID per request |
| **CORS** | Spring WebConfig | Configurable allowed origins |
| **Input Validation** | Bean Validation (JSR 380) | Request body & parameter validation |

---

## 🤖 AI-Powered Features

The system integrates **Google Gemini 2.0 Flash** for intelligent healthcare assistance:

### Symptom Analyzer
- Patients describe symptoms in natural language
- AI analyzes and suggests potential conditions
- Automatic department routing recommendation
- Confidence scoring for each suggestion

### Dual-Mode Operation
```
┌─────────────────────────────────────────┐
│          GEMINI_API_KEY set?            │
│                                         │
│    YES ──▶ 🤖 AI Mode                  │
│            Full Gemini analysis         │
│            Natural language processing  │
│                                         │
│    NO  ──▶ 📋 Demo Mode               │
│            Rule-based symptom matching  │
│            Keyword → department mapping │
└─────────────────────────────────────────┘
```

> **Privacy Note**: Patient symptoms are sent to the Gemini API for analysis but are not stored externally. All patient records remain in your database.

---

## 🗄 Database

### Dual Database Support

The system supports both **MySQL** and **PostgreSQL** with automatic dialect detection:

| Environment | Database | Driver |
|---|---|---|
| Local Development | MySQL 8 | `com.mysql.cj.jdbc.Driver` |
| Cloud (Render) | PostgreSQL 16 | `org.postgresql.Driver` |

### Schema Migration

Database versioning is managed by **Flyway** with vendor-specific migration scripts:

```
src/main/resources/db/migration/
├── mysql/
│   └── V1__init_schema.sql       # MySQL-specific DDL
└── postgresql/
    └── V1__init_schema.sql       # PostgreSQL-specific DDL
```

Flyway automatically detects the active database vendor and applies the correct migration scripts.

### Key Entities

```
┌──────────┐    ┌─────────────┐    ┌──────────────┐
│   User    │    │   Doctor    │    │   Patient    │
│──────────│    │─────────────│    │──────────────│
│ id       │◀──│ user_id     │    │ user_id      │──▶ User
│ email    │    │ department  │    │ blood_type   │
│ password │    │ polyclinic  │    │ birth_date   │
│ role     │    │ title       │    │ phone        │
└──────────┘    └──────┬──────┘    └──────┬───────┘
                       │                   │
                       ▼                   ▼
              ┌────────────────┐  ┌───────────────┐
              │  Appointment   │  │  Examination  │
              │────────────────│  │───────────────│
              │ doctor_id      │  │ appointment   │
              │ patient_id     │  │ diagnosis     │
              │ date / time    │  │ prescription  │
              │ status         │  │ notes         │
              └────────────────┘  └───────────────┘
```

---

## 🧪 Testing

### Test Infrastructure

| Type | Framework | Coverage |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Service layer |
| Integration Tests | Spring Boot Test | API endpoints |
| Security Tests | Spring Security Test | Auth & RBAC |
| Performance Tests | Custom analyzers | N+1 query detection, DB index verification |
| Test Database | H2 (in-memory) | Isolated test environment |

### Running Tests

```bash
# Run all tests
mvn test

# Run with verbose output
mvn test -Dspring.jpa.show-sql=true

# Run specific test class
mvn test -Dtest=AppointmentServiceTest
```

### Performance Testing

The project includes custom database performance analyzers:

- **`NPlusOneBaselineTest`** — Detects N+1 query problems
- **`DatabaseIndexAnalyzer`** — Analyzes index usage efficiency
- **`DatabaseIndexVerifier`** — Verifies required indexes exist

---

## ☁️ Deployment

### Render (Cloud)

The application is configured for one-click deployment on **Render**:

1. Connect your GitHub repository
2. Set environment variables (see [Environment Variables](#-environment-variables))
3. Render auto-detects the `Dockerfile` and builds
4. PostgreSQL addon for the database

### Docker Production Build

```bash
# Multi-stage build (build + runtime)
docker build -t hospital-system:latest .

# Run production container
docker run -d \
  --name hospital-system \
  -p 8080:8080 \
  -e JWT_SECRET="production-secret-key" \
  -e DB_URL="jdbc:postgresql://db-host:5432/hospitaldb" \
  -e DB_DRIVER="org.postgresql.Driver" \
  -e DB_USERNAME="prod_user" \
  -e DB_PASSWORD="prod_password" \
  hospital-system:latest
```

---

## ⚙️ CI/CD Pipeline

The project uses **GitHub Actions** for continuous integration:

```yaml
Triggers: push/PR → main, master
│
├── 🔨 Backend Build
│   ├── Setup JDK 17 (Temurin)
│   ├── Cache Maven dependencies
│   └── mvn clean package
│
└── 🎨 Frontend Build
    ├── Setup Node.js 18
    ├── Cache npm dependencies
    ├── npm ci / npm install
    └── npm run build
```

---

## 🔧 Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `JWT_SECRET` | ✅ | — | 256-bit secret key for JWT signing |
| `DB_URL` | ❌ | `jdbc:mysql://localhost:3306/hospitaldb...` | JDBC connection URL |
| `DB_DRIVER` | ❌ | `com.mysql.cj.jdbc.Driver` | Database driver class |
| `DB_USERNAME` | ❌ | `root` | Database username |
| `DB_PASSWORD` | ❌ | `12345678` | Database password |
| `DDL_AUTO` | ❌ | `validate` | Hibernate DDL strategy |
| `SHOW_SQL` | ❌ | `false` | Log SQL queries to console |
| `GEMINI_API_KEY` | ❌ | — | Google Gemini API key for AI features |
| `GEMINI_API_URL` | ❌ | `https://generativelanguage.googleapis.com/...` | Gemini API endpoint |
| `CORS_ALLOWED_ORIGINS` | ❌ | `http://localhost:3000` | Allowed CORS origins |
| `RATE_LIMIT_AUTH_CAPACITY` | ❌ | `10` | Max auth requests per window |
| `RATE_LIMIT_AUTH_MINUTES` | ❌ | `1` | Rate limit window (minutes) |

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'feat: add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

| Prefix | Description |
|---|---|
| `feat:` | New feature |
| `fix:` | Bug fix |
| `docs:` | Documentation |
| `refactor:` | Code refactoring |
| `test:` | Adding or updating tests |
| `chore:` | Maintenance tasks |

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ by [Akif Keklik](https://github.com/akifkeklik)**

<br/>

<sub>⭐ Star this repository if you found it helpful!</sub>

<br/>

[![GitHub](https://img.shields.io/badge/GitHub-akifkeklik-181717?style=for-the-badge&logo=github)](https://github.com/akifkeklik)

</div>
