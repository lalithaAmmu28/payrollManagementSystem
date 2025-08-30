-- Create job_roles table
CREATE TABLE job_roles (
    job_id VARCHAR(36) PRIMARY KEY,
    job_title VARCHAR(100) NOT NULL UNIQUE,
    base_salary DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create index for performance
CREATE INDEX idx_job_roles_title ON job_roles(job_title);
