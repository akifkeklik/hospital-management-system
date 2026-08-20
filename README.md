# Hospital Appointment & Management System 🏥

A modern, robust, and secure Hospital Information Management System (HIMS) built with **Java Spring Boot** and **Next.js**. Designed following Clean Architecture and Domain-Driven Design (Lite) principles, it provides a seamless, high-performance experience for patients, doctors, and hospital administrators.

## 🌟 Key Features

### 👨‍⚕️ Medical & Appointment Modules
- **Smart Appointment Management:** Calendar-based appointment scheduling, cancellation, and polyclinic assignment.
- **Automated Leave System:** When a doctor's leave request is approved by the admin, all conflicting appointments are automatically cancelled, and patients are notified instantly via the internal notification system.
- **Medical Records & Examination:** Dedicated queue screen for doctors, including ICD-10 diagnosis inputs and e-prescription modules. Patients can easily access their past medical history and test results.

### 🎨 Modern UI/UX & Interactivity
- **Global Smart Search:** A unified, live search bar accessible from anywhere. It intelligently searches through doctors, patients, and departments, providing instant suggestions and role badges.
- **T.C. Identity Card Scanner:** Support for barcode/QR code scanners. Admins can simply scan a patient's physical ID card to instantly pull up their profile or create a new registration without typing.
- **Dark Mode & Theming:** Full support for system-based and manual Light/Dark mode toggles with a premium, glassmorphism-inspired UI.
- **Multi-language Support (i18n):** Real-time language switching (Turkish / English) without page reloads.

### 🔒 Security & Architecture
- **Role-Based Access Control (RBAC):** Secure JWT authentication with distinct, isolated portals for Patients, Doctors, and Administrators.
- **Real-time Notifications:** Dynamic in-app notification system to keep users updated regarding their appointments and hospital announcements (broadcasts).
- **Pragmatic Layered Architecture:** Backend is structured using a Package-by-Feature (Domain) approach, separating logic cleanly across Controllers, Services, and Repositories.

## 🛠️ Technology Stack

**Backend (REST API)**
- **Java 17** & **Spring Boot 3**
- Spring Security (JWT Authentication)
- Spring Data JPA (Hibernate)
- PostgreSQL / MySQL
- Maven & Docker

**Frontend (Client)**
- **Next.js 14+** (React, App Router)
- Vanilla CSS Modules (Custom Design System, Zero generic frameworks)
- React Hot Toast (Notifications)
- Tesseract.js (OCR / Scanner integrations)

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Maven
- PostgreSQL (or any relational database)

### Backend Setup
1. Navigate to the root directory.
2. Update the database credentials in `src/main/resources/application.properties`.
3. Run the application:
```bash
mvn spring-boot:run
```
*The backend API will be available at `http://localhost:8080/api`.*

### Frontend Setup
1. Navigate to the frontend directory:
```bash
cd frontend
```
2. Copy the example environment file and configure it if necessary:
```bash
cp .env.example .env
```
3. Install dependencies:
```bash
npm install
```
3. Run the development server:
```bash
npm run dev
```
*The frontend will be available at `http://localhost:3000`.*

## 📦 Deployment
This application is cloud-ready and designed for zero-downtime deployments.
- **Backend:** Configured for deployment on platforms like Render or Railway using Docker and PostgreSQL.
- **Frontend:** Optimized for Vercel deployment with static generation and edge caching.

## 📄 License
This project is proprietary and intended for enterprise hospital management use.
