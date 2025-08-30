-- =================================================================
-- Payroll Management System - Initial Database Schema
-- Version: 1.0
-- Description: This script sets up all tables, relationships,
-- and constraints for the backend application.
-- =================================================================

-- Table: users
-- Stores login credentials and role information for system access.
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'EMPLOYEE') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table: departments
-- Stores the different departments within the organization.
CREATE TABLE departments (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table: job_roles
-- Defines the job titles and their corresponding base salaries.
CREATE TABLE job_roles (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    base_salary DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table: employees
-- Stores detailed personal and professional information for each employee.
CREATE TABLE employees (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    department_id VARCHAR(36) NOT NULL,
    job_role_id VARCHAR(36) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    phone VARCHAR(50),
    address TEXT,
    hire_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_employees_job_role FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE RESTRICT
);

-- Table: salary_structures
-- Manages historical and current salary components for each employee.
CREATE TABLE salary_structures (
    id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL,
    base_salary DECIMAL(12, 2) NOT NULL,
    bonus_details JSON,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_salary_structures_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Table: leave_requests
-- Tracks all leave applications made by employees.
CREATE TABLE leave_requests (
    id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL,
    leave_type ENUM('SICK', 'CASUAL', 'PAID') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_requests_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Table: payroll_runs
-- Stores metadata for each monthly payroll cycle.
CREATE TABLE payroll_runs (
    id VARCHAR(36) PRIMARY KEY,
    year INT NOT NULL,
    month INT NOT NULL,
    status ENUM('DRAFT', 'PROCESSED', 'LOCKED') NOT NULL DEFAULT 'DRAFT',
    processed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_payroll_run_period UNIQUE (year, month)
);

-- Table: payroll_items
-- Stores the calculated salary details for each employee for a single payroll run.
CREATE TABLE payroll_items (
    id VARCHAR(36) PRIMARY KEY,
    run_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    base_salary DECIMAL(12, 2) NOT NULL,
    bonus DECIMAL(12, 2) DEFAULT 0.00,
    deductions DECIMAL(12, 2) DEFAULT 0.00,
    net_salary DECIMAL(12, 2) NOT NULL,
    pay_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payroll_items_run FOREIGN KEY (run_id) REFERENCES payroll_runs(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_items_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uq_payroll_item_employee_run UNIQUE (run_id, employee_id)
);

-- =================================================================
-- Indexes for Performance Optimization
-- =================================================================
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_job_role_id ON employees(job_role_id);
CREATE INDEX idx_salary_structures_employee_id_effective_dates ON salary_structures(employee_id, effective_from, effective_to);
CREATE INDEX idx_leave_requests_employee_id_status ON leave_requests(employee_id, status);
CREATE INDEX idx_payroll_items_employee_id ON payroll_items(employee_id);
