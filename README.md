# Payroll Management System

## Overview
A full‑stack Payroll Management System that streamlines HR operations for small to mid‑sized organizations. It provides secure authentication, employee management, leave workflows, monthly payroll processing, and admin‑facing reporting.

## Features
- **Admin**
  - Employee management: create, update, delete, view details
  - Department and Job Role management
  - Leave request approval workflow (Pending/Approved/Rejected)
  - Payroll runs: create, process, re‑process, lock; view items and statistics
  - Reporting & analytics: department cost, leave trends, dashboard
  - Role‑based access control (ADMIN)
- **Employee**
  - Profile: view and update own details, change password
  - Leave: apply, view history, cancel pending requests
  - Payslips: view historical payslips per run
  - Role‑based access control (EMPLOYEE)

## Tech Stack
- **Backend**
  - Java 17, Spring Boot 3 (Web, Security, Validation)
  - JWT Authentication, RBAC
  - Spring Data JPA (Hibernate), Flyway migrations
  - MySQL
  - Testing: JUnit 5, Mockito, Spring MockMvc
  - Build: Maven (wrapper included)
- **Frontend**
  - React (Vite)
  - React Router, Axios, Formik, Yup
  - Bootstrap, React‑Bootstrap
  - Recharts (analytics)

## Prerequisites
- Java 17+
- Node.js 18+ and npm
- MySQL 8+ (running locally)
- Git

## How to Run Locally (Most Important)
1) Clone the repository
```bash
git clone <your-repo-url>.git
cd "Payroll Management System"
```

2) Configure the database
- Create a database named `payroll_db` in MySQL.
- Update backend DB credentials if needed in `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/payroll_db
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
```
Flyway will auto‑apply migrations on startup.

3) Start the backend (port 8080)
```bash
cd backend
mvn spring-boot:run
```
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

4) Start the frontend (port 3000)
```bash
cd ../frontend
npm install
npm run dev
```
- Frontend: `http://localhost:5173`

5) Optional: Run tests
```bash
# Backend unit + controller tests
cd backend
./mvnw test
```

## Accessing the Application
- Frontend: `http://localhost:5173`
- Backend Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Database: MySQL `payroll_db` on `localhost:3306` (use MySQL Workbench or CLI)

## Project Structure
```
Payroll Management System/
  backend/   # Spring Boot REST API, security, persistence, migrations, tests
  frontend/  # React app (Vite), routes, pages, layouts, components
  README.md
```

- `backend/` exposes REST endpoints under `/api/v1/...` and secures them with JWT + RBAC (ADMIN/EMPLOYEE).
- `frontend/` consumes these APIs and provides separate admin and employee experiences.
