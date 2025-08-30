-- Create payroll_runs table
CREATE TABLE payroll_runs (
    run_id VARCHAR(36) PRIMARY KEY,
    run_month INT NOT NULL,
    run_year INT NOT NULL,
    status ENUM('Draft', 'Processed', 'Locked') NOT NULL DEFAULT 'Draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_payroll_runs_month_year ON payroll_runs(run_year, run_month);
CREATE INDEX idx_payroll_runs_status ON payroll_runs(status);

-- Add constraints for valid month and year
ALTER TABLE payroll_runs 
ADD CONSTRAINT chk_payroll_month 
CHECK (run_month >= 1 AND run_month <= 12);

ALTER TABLE payroll_runs 
ADD CONSTRAINT chk_payroll_year 
CHECK (run_year >= 2020 AND run_year <= 2099);

-- Add unique constraint for month-year combination
ALTER TABLE payroll_runs 
ADD CONSTRAINT uk_payroll_month_year 
UNIQUE (run_month, run_year);
