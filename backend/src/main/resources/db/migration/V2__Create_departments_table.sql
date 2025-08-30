-- Create departments table
CREATE TABLE departments (
    department_id VARCHAR(36) PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create index for performance
CREATE INDEX idx_departments_name ON departments(department_name);
