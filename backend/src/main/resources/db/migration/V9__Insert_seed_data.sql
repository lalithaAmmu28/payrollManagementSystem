-- Comprehensive Seed Data for Payroll Management System
-- 
-- This seed data is designed to test ALL payroll scenarios:
--
-- BONUS CALCULATION SCENARIOS:
-- - e1 (Sarah): 10% percentage bonus
-- - e2 (John): Fixed $5000 bonus
-- - e3 (Jane): Fixed $7500 bonus (using "fixed" key)
-- - e4 (Bob): 15% percentage bonus
-- - e5 (Alice): No bonus (empty JSON)
-- - e6 (Charlie): Fixed $2000 bonus
-- - e7 (Diana): 5% percentage bonus
-- - e8 (Frank): Fixed $1500 bonus (using "fixed" key)
-- - e9 (Grace): 20% percentage bonus (high performer)
--
-- LOSS OF PAY DEDUCTION SCENARIOS:
-- February 2024:
-- - e3: 3 days sick leave (unpaid) = deduction
-- - e6: 2 days sick leave (unpaid) = deduction
-- - e8: 3 days sick leave (unpaid) = deduction
-- - e2: Partial sick leave spanning Jan-Feb = partial deduction
-- - e9: Partial casual leave spanning Feb-Mar = partial deduction
-- - e5: Paid leave = NO deduction
--
-- March 2024 (Draft - ready for processing tests):
-- - e4: 3 days casual leave (unpaid) = will cause deduction
-- - e7: 2 days casual leave (unpaid) = will cause deduction
-- - e9: Partial casual leave from Feb-Mar span = partial deduction
--
-- PAYROLL RUN WORKFLOW SCENARIOS:
-- - pr1 (Jan 2024): Locked status - employees can access payslips
-- - pr2 (Feb 2024): Processed status - ready for lock testing
-- - pr3 (Mar 2024): Draft status - ready for process testing
-- - pr4 (Apr 2024): Draft status - additional testing

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
('u1', 'admin', 'admin@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Admin', TRUE, NOW(), NOW()),
('u2', 'hr.manager', 'hr.manager@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Admin', TRUE, NOW(), NOW());

-- Employee users
INSERT INTO users (user_id, username, email, password, role, is_active, created_at, updated_at) VALUES
('u3', 'john.doe', 'john.doe@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW()),
('u4', 'jane.smith', 'jane.smith@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW()),
('u5', 'bob.johnson', 'bob.johnson@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW()),
('u6', 'alice.brown', 'alice.brown@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW()),
('u7', 'charlie.davis', 'charlie.davis@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW()),
('u8', 'diana.wilson', 'diana.wilson@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW()),
('u9', 'frank.miller', 'frank.miller@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW()),
('u10', 'grace.taylor', 'grace.taylor@company.com', '$2a$12$Nn6oBol/8weI4BwIG5jRw.VMXmHGf19y13LpBzJgJFxn3KOAyzLAC', 'Employee', TRUE, NOW(), NOW());

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

-- Insert Salary Structures (current salaries with diverse bonus types)
-- Testing different bonus calculation scenarios

-- Scenario 1: Percentage-based bonus (10% performance bonus)
INSERT INTO salary_structures (structure_id, employee_id, base_salary, bonus_details, effective_from, effective_to, created_at, updated_at) VALUES
('s1', 'e1', 75000.00, '{"percentage": 10}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 2: Fixed amount bonus
('s2', 'e2', 80000.00, '{"amount": 5000}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 3: Alternative fixed bonus key
('s3', 'e3', 100000.00, '{"fixed": 7500}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 4: Percentage-based bonus (15% performance bonus)
('s4', 'e4', 90000.00, '{"percentage": 15}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 5: No bonus (empty JSON)
('s5', 'e5', 65000.00, '{}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 6: Fixed amount bonus (lower amount)
('s6', 'e6', 50000.00, '{"amount": 2000}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 7: Percentage-based bonus (5% conservative bonus)
('s7', 'e7', 70000.00, '{"percentage": 5}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 8: Fixed amount bonus (modest amount)
('s8', 'e8', 45000.00, '{"fixed": 1500}', '2024-01-01', NULL, NOW(), NOW()),

-- Scenario 9: High percentage bonus (20% for sales manager)
('s9', 'e9', 85000.00, '{"percentage": 20}', '2024-01-01', NULL, NOW(), NOW());

-- Historical salary structures to test overlapping periods
INSERT INTO salary_structures (structure_id, employee_id, base_salary, bonus_details, effective_from, effective_to, created_at, updated_at) VALUES
-- Sarah's previous salary (to test salary history)
('s10', 'e1', 70000.00, '{"percentage": 8}', '2023-01-01', '2023-12-31', NOW(), NOW()),

-- John's promotion history
('s11', 'e2', 75000.00, '{"amount": 4000}', '2023-06-01', '2023-12-31', NOW(), NOW()),

-- Jane's promotion with significant increase
('s12', 'e3', 85000.00, '{"fixed": 5000}', '2023-01-01', '2023-12-31', NOW(), NOW());

-- Insert Comprehensive Leave Requests for Payroll Testing
INSERT INTO leave_requests (leave_id, employee_id, leave_type, start_date, end_date, status, reason, created_at, updated_at) VALUES
-- Paid leaves (should NOT affect payroll deduction)
('l1', 'e2', 'Paid', '2024-03-15', '2024-03-17', 'Approved', 'Family vacation', NOW(), NOW()),
('l2', 'e5', 'Paid', '2024-02-10', '2024-02-12', 'Approved', 'Personal vacation', NOW(), NOW()),

-- UNPAID SICK leaves (SHOULD affect payroll deduction for February)
('l3', 'e3', 'Sick', '2024-02-20', '2024-02-22', 'Approved', 'Medical appointment', NOW(), NOW()),
('l4', 'e6', 'Sick', '2024-02-15', '2024-02-16', 'Approved', 'Flu symptoms', NOW(), NOW()),
('l5', 'e8', 'Sick', '2024-02-05', '2024-02-07', 'Approved', 'Cold symptoms', NOW(), NOW()),

-- UNPAID CASUAL leaves (SHOULD affect payroll deduction for March)
('l6', 'e4', 'Casual', '2024-03-10', '2024-03-12', 'Approved', 'Personal matters', NOW(), NOW()),
('l7', 'e7', 'Casual', '2024-03-25', '2024-03-26', 'Approved', 'Family emergency', NOW(), NOW()),

-- More UNPAID leaves spanning different periods to test overlap calculation
('l8', 'e2', 'Sick', '2024-01-28', '2024-02-02', 'Approved', 'Recovery period', NOW(), NOW()),  -- Spans January-February
('l9', 'e9', 'Casual', '2024-02-28', '2024-03-01', 'Approved', 'Moving day', NOW(), NOW()),    -- Spans February-March

-- Pending and rejected leaves (should NOT affect payroll)
('l10', 'e1', 'Sick', '2024-03-05', '2024-03-06', 'Pending', 'Waiting for approval', NOW(), NOW()),
('l11', 'e3', 'Casual', '2024-04-15', '2024-04-17', 'Rejected', 'Not approved', NOW(), NOW()),

-- Additional test cases for April (future payroll run)
('l12', 'e5', 'Sick', '2024-04-10', '2024-04-12', 'Approved', 'Medical procedure', NOW(), NOW()),
('l13', 'e8', 'Casual', '2024-04-20', '2024-04-21', 'Approved', 'Personal day', NOW(), NOW());

-- Insert Sample Payroll Runs with proper workflow status and timestamps
INSERT INTO payroll_runs (run_id, run_month, run_year, status, created_at, updated_at, processed_at, locked_at) VALUES
-- January 2024: Fully completed (processed and locked)
('pr1', 1, 2024, 'Locked', '2024-01-25 10:00:00', '2024-01-31 15:30:00', '2024-01-30 14:00:00', '2024-01-31 15:30:00'),

-- February 2024: Processed but not locked yet (ready for testing lock functionality)
('pr2', 2, 2024, 'Processed', '2024-02-25 09:00:00', '2024-02-28 16:45:00', '2024-02-28 16:45:00', NULL),

-- March 2024: Draft status (ready for testing processing)
('pr3', 3, 2024, 'Draft', '2024-03-25 11:30:00', '2024-03-25 11:30:00', NULL, NULL),

-- April 2024: Another draft for additional testing
('pr4', 4, 2024, 'Draft', '2024-04-25 10:15:00', '2024-04-25 10:15:00', NULL, NULL);

-- Insert Realistic Payroll Items Based on New Salary Structures

-- January 2024 Payroll Items (Locked - employees can access these payslips)
-- Calculated based on monthly salaries with new bonus structures
INSERT INTO payroll_items (item_id, run_id, employee_id, base_salary, bonus, deductions, net_salary, pay_date, created_at, updated_at) VALUES
-- e1: Sarah (75000/12 = 6250.00, 10% bonus = 625.00, no deductions)
('pi1', 'pr1', 'e1', 6250.00, 625.00, 0.00, 6875.00, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e2: John (80000/12 = 6666.67, fixed 5000 bonus, partial sick leave deduction from spanning leave)
('pi2', 'pr1', 'e2', 6666.67, 5000.00, 645.16, 11021.51, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e3: Jane (100000/12 = 8333.33, fixed 7500 bonus, no deductions)
('pi3', 'pr1', 'e3', 8333.33, 7500.00, 0.00, 15833.33, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e4: Bob (90000/12 = 7500.00, 15% bonus = 1125.00, no deductions)
('pi4', 'pr1', 'e4', 7500.00, 1125.00, 0.00, 8625.00, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e5: Alice (65000/12 = 5416.67, no bonus, no deductions)
('pi5', 'pr1', 'e5', 5416.67, 0.00, 0.00, 5416.67, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e6: Charlie (50000/12 = 4166.67, fixed 2000 bonus, no deductions)
('pi6', 'pr1', 'e6', 4166.67, 2000.00, 0.00, 6166.67, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e7: Diana (70000/12 = 5833.33, 5% bonus = 291.67, no deductions)
('pi7', 'pr1', 'e7', 5833.33, 291.67, 0.00, 6125.00, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e8: Frank (45000/12 = 3750.00, fixed 1500 bonus, no deductions)
('pi8', 'pr1', 'e8', 3750.00, 1500.00, 0.00, 5250.00, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00'),

-- e9: Grace (85000/12 = 7083.33, 20% bonus = 1416.67, no deductions)
('pi9', 'pr1', 'e9', 7083.33, 1416.67, 0.00, 8500.00, '2024-01-31', '2024-01-30 14:30:00', '2024-01-30 14:30:00');

-- February 2024 Payroll Items (Processed but not locked - for testing lock functionality)
-- These include loss of pay deductions for unpaid sick leaves
INSERT INTO payroll_items (item_id, run_id, employee_id, base_salary, bonus, deductions, net_salary, pay_date, created_at, updated_at) VALUES
-- e1: Sarah (no leaves in Feb)
('pi10', 'pr2', 'e1', 6250.00, 625.00, 0.00, 6875.00, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e2: John (with partial sick leave deduction from spanning leave l8)
('pi11', 'pr2', 'e2', 6666.67, 5000.00, 473.33, 11193.34, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e3: Jane (3 days sick leave: 8333.33/29 * 3 = 862.07)
('pi12', 'pr2', 'e3', 8333.33, 7500.00, 862.07, 14971.26, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e4: Bob (no leaves in Feb)
('pi13', 'pr2', 'e4', 7500.00, 1125.00, 0.00, 8625.00, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e5: Alice (has paid leave, no deduction)
('pi14', 'pr2', 'e5', 5416.67, 0.00, 0.00, 5416.67, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e6: Charlie (2 days sick leave: 4166.67/29 * 2 = 287.36)
('pi15', 'pr2', 'e6', 4166.67, 2000.00, 287.36, 5879.31, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e7: Diana (no leaves in Feb)
('pi16', 'pr2', 'e7', 5833.33, 291.67, 0.00, 6125.00, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e8: Frank (3 days sick leave: 3750.00/29 * 3 = 387.93)
('pi17', 'pr2', 'e8', 3750.00, 1500.00, 387.93, 4862.07, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00'),

-- e9: Grace (partial casual leave from spanning leave l9: 1 day in Feb)
('pi18', 'pr2', 'e9', 7083.33, 1416.67, 244.25, 8255.75, NULL, '2024-02-28 16:45:00', '2024-02-28 16:45:00');
