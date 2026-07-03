# Hospital Appointment & Management System 🏥

A modern, robust, and secure Hospital Information Management System (HIMS) built with Java Spring Boot and Next.js. Designed following Clean Architecture principles, it provides a seamless experience for patients, doctors, and administrators.

## 🌟 Key Features

- **Smart Appointment Management:** Calendar-based appointment scheduling, cancellation, and polyclinic management.
- **Automated Leave System:** When a doctor's leave request is approved, all their conflicting appointments are automatically cancelled, and patients are notified instantly.
- **Medical Records & Examination:** Dedicated queue screen for doctors, including ICD-10 diagnosis and e-prescription modules. Patients can view their complete medical history.
- **Real-time Notifications:** Dynamic in-app notification system to keep patients and doctors updated regarding their appointments and hospital announcements.
- **Role-Based Access Control:** Secure JWT authentication with distinct portals for Patients, Doctors, and Administrators.

## 🛠️ Technology Stack

**Backend**
- Java 17
- Spring Boot 3
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL / MySQL
- Maven & Docker

**Frontend**
- Next.js (React)
- Vanilla CSS Modules (Custom Design System)
- React Hot Toast
- Fetch API

## 🚀 Getting Started

### Prerequisites
- Java 17
- Node.js 18+
- Maven
- PostgreSQL or MySQL

### Backend Setup
1. Navigate to the root directory.
2. Update the database credentials in `src/main/resources/application.properties`.
3. Run the application:
```bash
mvn spring-boot:run
```
The backend API will be available at `http://localhost:8080`.

### Frontend Setup
1. Navigate to the frontend directory:
```bash
cd frontend
```
2. Install dependencies:
```bash
npm install
```
3. Run the development server:
```bash
npm run dev
```
The frontend will be available at `http://localhost:3000`.

## 📦 Deployment
This application is fully Dockerized and cloud-ready. 
- The backend is configured for deployment on Render using Docker and PostgreSQL.
- The frontend is optimized for deployment on Vercel.

## 📄 License
This project is proprietary and intended for enterprise use.
