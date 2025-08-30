-- Seed data for Payroll Management System

-- Clear existing data (in reverse order of dependencies)
DELETE FROM payroll_items;
DELETE FROM payroll_runs;
DELETE FROM leave_requests;
DELETE FROM salary_structures;
DELETE FROM employees;
DELETE FROM job_roles;
DELETE FROM departments;
DELETE FROM users;

-- Insert Departments
INSERT INTO departments (department_id, department_name, created_at, updated_at) VALUES
('d1', 'Human Resources', NOW(), NOW()),
('d2', 'Information Technology', NOW(), NOW()),
('d3', 'Finance', NOW(), NOW()),
('d4', 'Marketing', NOW(), NOW()),
('d5', 'Sales', NOW(), NOW());

-- Insert Job Roles
INSERT INTO job_roles (job_id, job_title, base_salary, created_at, updated_at) VALUES
('j1', 'HR Manager', 75000.00, NOW(), NOW()),
('j2', 'HR Specialist', 55000.00, NOW(), NOW()),
('j3', 'Software Engineer', 80000.00, NOW(), NOW()),
('j4', 'Senior Software Engineer', 100000.00, NOW(), NOW()),
('j5', 'IT Manager', 90000.00, NOW(), NOW()),
('j6', 'Financial Analyst', 65000.00, NOW(), NOW()),
('j7', 'Accountant', 50000.00, NOW(), NOW()),
('j8', 'Marketing Manager', 70000.00, NOW(), NOW()),
('j9', 'Marketing Specialist', 50000.00, NOW(), NOW()),
('j10', 'Sales Representative', 45000.00, NOW(), NOW()),
('j11', 'Sales Manager', 85000.00, NOW(), NOW());

-- Insert Users (passwords are 'password123' hashed with BCrypt)
-- Admin users
INSERT INTO users (user_id, username, email, password, role, is_active, created_at, updated_at) VALUES
('u1', 'admin', 'admin@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Admin', TRUE, NOW(), NOW()),
('u2', 'hr.manager', 'hr.manager@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Admin', TRUE, NOW(), NOW());

-- Employee users
INSERT INTO users (user_id, username, email, password, role, is_active, created_at, updated_at) VALUES
('u3', 'john.doe', 'john.doe@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW()),
('u4', 'jane.smith', 'jane.smith@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW()),
('u5', 'bob.johnson', 'bob.johnson@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW()),
('u6', 'alice.brown', 'alice.brown@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW()),
('u7', 'charlie.davis', 'charlie.davis@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW()),
('u8', 'diana.wilson', 'diana.wilson@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW()),
('u9', 'frank.miller', 'frank.miller@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW()),
('u10', 'grace.taylor', 'grace.taylor@company.com', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.H24dyORQgBpxMEmtcKmOHWaK', 'Employee', TRUE, NOW(), NOW());

-- Insert Employees
INSERT INTO employees (employee_id, user_id, job_id, department_id, first_name, last_name, date_of_birth, phone, address, leave_balance, created_at, updated_at) VALUES
('e1', 'u2', 'j1', 'd1', 'Sarah', 'Johnson', '1985-03-15', '+1-555-0101', '123 Main St, Anytown, AT 12345', 20.00, NOW(), NOW()),
('e2', 'u3', 'j3', 'd2', 'John', 'Doe', '1990-06-20', '+1-555-0102', '456 Oak Ave, Somewhere, ST 67890', 25.00, NOW(), NOW()),
('e3', 'u4', 'j4', 'd2', 'Jane', 'Smith', '1988-09-10', '+1-555-0103', '789 Pine Rd, Elsewhere, EL 54321', 22.00, NOW(), NOW()),
('e4', 'u5', 'j5', 'd2', 'Bob', 'Johnson', '1982-12-05', '+1-555-0104', '321 Elm St, Nowhere, NW 98765', 18.00, NOW(), NOW()),
('e5', 'u6', 'j6', 'd3', 'Alice', 'Brown', '1992-01-25', '+1-555-0105', '654 Maple Dr, Anyplace, AP 13579', 30.00, NOW(), NOW()),
('e6', 'u7', 'j7', 'd3', 'Charlie', 'Davis', '1989-04-30', '+1-555-0106', '987 Birch Ln, Someplace, SP 24680', 15.00, NOW(), NOW()),
('e7', 'u8', 'j8', 'd4', 'Diana', 'Wilson', '1987-08-12', '+1-555-0107', '147 Cedar Ave, Wherever, WH 11111', 28.00, NOW(), NOW()),
('e8', 'u9', 'j10', 'd5', 'Frank', 'Miller', '1991-11-08', '+1-555-0108', '258 Spruce St, Anywhere, AW 22222', 12.00, NOW(), NOW()),
('e9', 'u10', 'j11', 'd5', 'Grace', 'Taylor', '1986-02-18', '+1-555-0109', '369 Fir Ct, Elsewhere, EL 33333', 35.00, NOW(), NOW());

-- Insert Salary Structures (current salaries)
INSERT INTO salary_structures (structure_id, employee_id, base_salary, bonus_details, effective_from, effective_to, created_at, updated_at) VALUES
('s1', 'e1', 75000.00, '{"performance_bonus": 5000, "annual_bonus": 3000}', '2024-01-01', NULL, NOW(), NOW()),
('s2', 'e2', 80000.00, '{"performance_bonus": 4000, "annual_bonus": 2000}', '2024-01-01', NULL, NOW(), NOW()),
('s3', 'e3', 100000.00, '{"performance_bonus": 8000, "annual_bonus": 5000}', '2024-01-01', NULL, NOW(), NOW()),
('s4', 'e4', 90000.00, '{"performance_bonus": 6000, "annual_bonus": 4000}', '2024-01-01', NULL, NOW(), NOW()),
('s5', 'e5', 65000.00, '{"performance_bonus": 3000, "annual_bonus": 2000}', '2024-01-01', NULL, NOW(), NOW()),
('s6', 'e6', 50000.00, '{"performance_bonus": 2000, "annual_bonus": 1000}', '2024-01-01', NULL, NOW(), NOW()),
('s7', 'e7', 70000.00, '{"performance_bonus": 4000, "annual_bonus": 3000}', '2024-01-01', NULL, NOW(), NOW()),
('s8', 'e8', 45000.00, '{"performance_bonus": 2000, "annual_bonus": 1000}', '2024-01-01', NULL, NOW(), NOW()),
('s9', 'e9', 85000.00, '{"performance_bonus": 5000, "annual_bonus": 4000}', '2024-01-01', NULL, NOW(), NOW());

-- Insert Sample Leave Requests
INSERT INTO leave_requests (leave_id, employee_id, leave_type, start_date, end_date, status, reason, created_at, updated_at) VALUES
('l1', 'e2', 'Paid', '2024-03-15', '2024-03-17', 'Pending', 'Family vacation', NOW(), NOW()),
('l2', 'e3', 'Sick', '2024-02-20', '2024-02-22', 'Approved', 'Medical appointment', NOW(), NOW()),
('l3', 'e5', 'Casual', '2024-04-10', '2024-04-12', 'Pending', 'Personal matters', NOW(), NOW()),
('l4', 'e6', 'Paid', '2024-05-01', '2024-05-05', 'Rejected', 'Extended vacation', NOW(), NOW()),
('l5', 'e8', 'Sick', '2024-01-25', '2024-01-26', 'Approved', 'Flu symptoms', NOW(), NOW());

-- Insert Sample Payroll Runs
INSERT INTO payroll_runs (run_id, run_month, run_year, status, created_at, updated_at) VALUES
('pr1', 1, 2024, 'Processed', NOW(), NOW()),
('pr2', 2, 2024, 'Processed', NOW(), NOW()),
('pr3', 3, 2024, 'Draft', NOW(), NOW());

-- Insert Sample Payroll Items for January 2024
INSERT INTO payroll_items (item_id, run_id, employee_id, base_salary, bonus, deductions, net_salary, pay_date, created_at, updated_at) VALUES
('pi1', 'pr1', 'e1', 6250.00, 500.00, 625.00, 6125.00, '2024-01-31', NOW(), NOW()),
('pi2', 'pr1', 'e2', 6666.67, 400.00, 666.67, 6400.00, '2024-01-31', NOW(), NOW()),
('pi3', 'pr1', 'e3', 8333.33, 800.00, 833.33, 8300.00, '2024-01-31', NOW(), NOW()),
('pi4', 'pr1', 'e4', 7500.00, 600.00, 750.00, 7350.00, '2024-01-31', NOW(), NOW()),
('pi5', 'pr1', 'e5', 5416.67, 300.00, 541.67, 5175.00, '2024-01-31', NOW(), NOW()),
('pi6', 'pr1', 'e6', 4166.67, 200.00, 416.67, 3950.00, '2024-01-31', NOW(), NOW()),
('pi7', 'pr1', 'e7', 5833.33, 400.00, 583.33, 5650.00, '2024-01-31', NOW(), NOW()),
('pi8', 'pr1', 'e8', 3750.00, 200.00, 375.00, 3575.00, '2024-01-31', NOW(), NOW()),
('pi9', 'pr1', 'e9', 7083.33, 500.00, 708.33, 6875.00, '2024-01-31', NOW(), NOW());
