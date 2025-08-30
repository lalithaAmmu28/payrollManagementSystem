-- Create payroll_items table
CREATE TABLE payroll_items (
    item_id VARCHAR(36) PRIMARY KEY,
    run_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    base_salary DECIMAL(12,2) NOT NULL,
    bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    deductions DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    net_salary DECIMAL(12,2) NOT NULL,
    pay_date DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (run_id) REFERENCES payroll_runs(run_id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_payroll_items_run_id ON payroll_items(run_id);
CREATE INDEX idx_payroll_items_employee_id ON payroll_items(employee_id);
CREATE INDEX idx_payroll_items_pay_date ON payroll_items(pay_date);

-- Add unique constraint to prevent duplicate payroll items for same employee in same run
ALTER TABLE payroll_items 
ADD CONSTRAINT uk_payroll_items_run_employee 
UNIQUE (run_id, employee_id);
